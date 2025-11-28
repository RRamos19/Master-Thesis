package thesis.model.persistence;

import org.hibernate.Session;
import thesis.model.domain.InMemoryRepository;
import thesis.model.domain.components.*;
import thesis.model.persistence.entities.*;
import thesis.model.persistence.entities.utils.*;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DataMerger {
    private final Session session;
    private final ConstraintTypeEntityFactory constraintTypeEntityFactory;
    private final RoomEntityFactory roomEntityFactory;
    private final TimeBlockEntityFactory timeBlockEntityFactory;
    private final TeacherEntityFactory teacherEntityFactory;
    private final CourseNameEntityFactory courseNameEntityFactory;
    private final ConfigNameEntityFactory configNameEntityFactory;
    private final SubpartNameEntityFactory subpartNameEntityFactory;
    private final ClassUnitNameEntityFactory classUnitNameEntityFactory;

    private final Map<String, ClassUnitEntity> classUnitMap = new HashMap<>();

    public DataMerger(Session session, RoomEntityFactory roomEntityFactory, TeacherEntityFactory teacherEntityFactory) {
        this.session = session;
        this.constraintTypeEntityFactory = new ConstraintTypeEntityFactory(session);
        this.timeBlockEntityFactory = new TimeBlockEntityFactory(session);
        this.roomEntityFactory = roomEntityFactory;
        this.teacherEntityFactory = teacherEntityFactory;
        this.courseNameEntityFactory = new CourseNameEntityFactory(session);
        this.configNameEntityFactory = new ConfigNameEntityFactory(session);
        this.subpartNameEntityFactory = new SubpartNameEntityFactory(session);
        this.classUnitNameEntityFactory = new ClassUnitNameEntityFactory(session);
    }

    public void mergeClasses(ProgramEntity programEntity, InMemoryRepository inMemoryRepository) {
        mergeCollections(
            inMemoryRepository.getCourses(),
            programEntity.getCourseEntitySet(),
            Course::getCourseId,
            (courseEntity) -> courseEntity.getCourseNameEntity().getName(),
            this::mergeCourses,
            (course) -> createCourseEntity(programEntity, course),
            (courseEntity) -> programEntity.getCourseEntitySet().remove(courseEntity)
        );

        mergeCollections(
            inMemoryRepository.getConstraints(),
            programEntity.getConstraintEntitySet(),
            Constraint::getId,
            (constraintEntity) -> constraintEntity.getId().getConstraintPK(),
            this::mergeConstraints,
            (constraint) -> createConstraintEntity(programEntity, constraint),
            (constraintEntity) -> programEntity.getConstraintEntitySet().remove(constraintEntity)
        );
    }

    private static <T, E, K> void mergeCollections(
            Collection<T> source,
            Collection<E> target,
            Function<T, K> sourceKey,
            Function<E, K> targetKey,
            BiConsumer<T, E> mergeExisting,
            Consumer<T> createNew,
            Consumer<E> removeOrphan) {

        // More efficient for searching matches in keys
        Map<K, E> targetMap = target.stream()
            .collect(Collectors.toMap(targetKey, e -> e));

        // Search each value in source to add to the database
        for (T src : source) {
            K key = sourceKey.apply(src);
            E tgt = targetMap.remove(key);

            // If an equivalent entity is found then a merge occurs, otherwise a new entity is created
            if (tgt != null) {
                mergeExisting.accept(src, tgt);
            } else {
                createNew.accept(src);
            }
        }

        // The target leftovers must be removed
        for (E orphan : targetMap.values()) {
            removeOrphan.accept(orphan);
        }
    }

    private void createCourseEntity(ProgramEntity programEntity, Course course) {
        CourseEntity courseEntity = new CourseEntity(programEntity, courseNameEntityFactory.getOrCreateCourseName(course.getCourseId()));
        session.persist(courseEntity);

        for(Config config : course.getConfigList()) {
            createConfigEntity(courseEntity, config);
        }
    }

    private void mergeCourses(Course course, CourseEntity courseEntity) {
        mergeCollections(
            course.getConfigList(),
            courseEntity.getConfigSet(),
            Config::getConfigId,
            (configEntity) -> configEntity.getConfigNameEntity().getName(),
            this::mergeConfigs,
            (config) -> createConfigEntity(courseEntity, config),
            (configEntity -> courseEntity.getConfigSet().remove(configEntity))
        );
    }

    private void createConfigEntity(CourseEntity courseEntity, Config config) {
        ConfigEntity configEntity = new ConfigEntity(courseEntity, configNameEntityFactory.getOrCreateConfigName(config.getConfigId()));
        session.persist(configEntity);

        for(Subpart subpart : config.getSubpartList()) {
            createSubpartEntity(configEntity, subpart);
        }
    }

    private void mergeConfigs(Config config, ConfigEntity configEntity) {
        mergeCollections(
            config.getSubpartList(),
            configEntity.getSubpartSet(),
            Subpart::getSubpartId,
            (subpartEntity) -> subpartEntity.getSubpartNameEntity().getName(),
            this::mergeSubparts,
            (subpart) -> createSubpartEntity(configEntity, subpart),
            (subpartEntity -> configEntity.getSubpartSet().remove(subpartEntity))
        );
    }

    private void createSubpartEntity(ConfigEntity configEntity, Subpart subpart) {
        SubpartEntity subpartEntity = new SubpartEntity(configEntity, subpartNameEntityFactory.getOrCreateSubpartName(subpart.getSubpartId()));
        session.persist(subpartEntity);

        for(ClassUnit cls : subpart.getClassUnitList()) {
            createClassUnitEntity(subpartEntity, cls);
        }
    }

    private void mergeSubparts(Subpart subpart, SubpartEntity subpartEntity) {
        mergeCollections(
            subpart.getClassUnitList(),
            subpartEntity.getClassUnitSet(),
            ClassUnit::getClassId,
            (classUnit) -> classUnit.getClassUnitNameEntity().getName(),
            this::mergeClassUnits,
            (classUnit) -> createClassUnitEntity(subpartEntity, classUnit),
            (classUnitEntity -> subpartEntity.getClassUnitSet().remove(classUnitEntity))
        );
    }

    private void createClassUnitEntity(SubpartEntity subpartEntity, ClassUnit classUnit) {
        ClassUnitEntity classUnitEntity = new ClassUnitEntity(subpartEntity, classUnitNameEntityFactory.getOrCreateClassUnitName(classUnit.getClassId()));
        session.persist(classUnitEntity);

        classUnit.getClassTimePenalties().forEach((time, penalty) -> {
            new ClassTimeEntity(
                classUnitEntity,
                timeBlockEntityFactory.getOrCreate(time.getStartSlot(), time.getLength(), time.getDays(), time.getWeeks()),
                penalty);
        });

        classUnit.getClassRoomPenalties().forEach((roomId, penalty) -> {
            RoomEntity roomEntity = roomEntityFactory.getOrCreateRoom(roomId);
            new ClassRoomEntity(classUnitEntity, roomEntity, penalty);
        });

        classUnit.getTeacherIdList().forEach((teacherId) -> {
            TeacherEntity teacherEntity = teacherEntityFactory.get(teacherId);
            new TeacherClassEntity(classUnitEntity, teacherEntity);
        });

        // Add the class to a map to make the merge of constraint more efficient
        classUnitMap.put(classUnitEntity.getClassUnitNameEntity().getName(), classUnitEntity);
    }

    private List<Object> createTimeId(Time time) {
        return List.of(time.getStartSlot(), time.getLength(), time.getDays(), time.getWeeks());
    }

    private List<Object> createTimeBlockId(TimeBlockEntity timeBlockEntity) {
        return List.of(timeBlockEntity.getStartSlot(), timeBlockEntity.getDuration(), timeBlockEntity.getDays(), timeBlockEntity.getWeeks());
    }

    private void mergeClassUnits(ClassUnit classUnit, ClassUnitEntity classUnitEntity) {
        // More efficient for searching matches in keys
        Map<Integer, TeacherClassEntity> teacherMap = classUnitEntity.getTeacherClassEntitySet().stream()
                .collect(Collectors.toMap(e -> e.getTeacherEntity().getId(), e -> e));
        Map<String, ClassRoomEntity> classRoomMap = classUnitEntity.getClassRoomEntitySet().stream()
                .collect(Collectors.toMap(classRoomEntity -> classRoomEntity.getRoom().getName(), e -> e));
        Map<List<Object>, ClassTimeEntity> classTimeMap = classUnitEntity.getClassTimeEntitySet().stream()
                .collect(Collectors.toMap(e -> createTimeBlockId(e.getTimeBlockEntity()), e -> e));

        // Merge class times
        classUnit.getClassTimePenalties().forEach((time, penalty) -> {
            List<Object> timeId = createTimeId(time);
            if(!classTimeMap.containsKey(timeId)) {
                new ClassTimeEntity(
                    classUnitEntity,
                    timeBlockEntityFactory.getOrCreate(time.getStartSlot(), time.getLength(), time.getDays(), time.getWeeks()),
                    penalty);
            } else {
                ClassTimeEntity classTimeEntity = classTimeMap.get(timeId);

                // Update penalty
                classTimeEntity.setPenalty(penalty);
            }

            classTimeMap.remove(timeId);
        });
        // Remove the time leftovers
        classTimeMap.values().forEach((classTime) -> {
            classTime.getClassUnit().getClassTimeEntitySet().remove(classTime);
        });

        // Merge classrooms
        classUnit.getClassRoomPenalties().forEach((roomId, penalty) -> {
            if(!classRoomMap.containsKey(roomId)) {
                RoomEntity roomEntity = roomEntityFactory.getOrCreateRoom(roomId);
                new ClassRoomEntity(classUnitEntity, roomEntity, penalty);
            }

            classRoomMap.remove(roomId);
        });
        // Remove classRoom leftovers
        classRoomMap.values().forEach((classRoom) -> {
            classRoom.getClassUnit().getClassRoomEntitySet().remove(classRoom);
        });

        // Merge teacher ids
        classUnit.getTeacherIdList().forEach(teacherId -> {
            if(!teacherMap.containsKey(teacherId)) {
                new TeacherClassEntity(classUnitEntity, teacherEntityFactory.get(teacherId));
            }

            teacherMap.remove(teacherId);
        });
        // Remove teacher leftovers
        teacherMap.values().forEach(classUnitEntity::removeTeacherClass);

        // Add the class to a map to make the merge of constraint more efficient
        classUnitMap.put(classUnitEntity.getClassUnitNameEntity().getName(), classUnitEntity);
    }

    private void createConstraintEntity(ProgramEntity programEntity, Constraint constraint) {
        ConstraintEntity constraintEntity = new ConstraintEntity(
            constraint.getId(),
            programEntity,
            constraintTypeEntityFactory.getConstraintType(constraint.getType()),
            constraint.getFirstParameter(),
            constraint.getSecondParameter(),
            constraint.getPenalty(),
            constraint.getRequired());

        session.persist(constraintEntity);

        // Add all the classes to the constraintEntity
        constraint.getClassUnitIdList().forEach((classId) -> {
            new ClassConstraintEntity(
                classUnitMap.get(classId),
                constraintEntity);
        });
    }

    private void mergeConstraints(Constraint constraint, ConstraintEntity constraintEntity) {
        // Set all the values pertaining to the constraint
        constraintEntity.setConstraintTypeEntity(constraintTypeEntityFactory.getConstraintType(constraint.getType()));
        constraintEntity.setFirst_parameter(constraint.getFirstParameter());
        constraintEntity.setSecond_parameter(constraintEntity.getSecond_parameter());
        constraintEntity.setPenalty(constraintEntity.getPenalty());
        constraintEntity.setRequired(constraint.getRequired());

        // Start of the merge of the classes associated to the constraint
        Map<Integer, ClassConstraintEntity> classConstraintMap = constraintEntity.getClassConstraintEntityList().stream()
                .collect(Collectors.toMap((e) -> e.getId().getClassUnitId(), e -> e));

        // Check if the classes are the same in both constraint objects
        // If not, the constraintEntity will remove the ones not found in Constraint and will create the ones missing
        constraint.getClassUnitIdList().forEach((classId) -> {
            ClassUnitEntity classUnitEntity = classUnitMap.get(classId);
            if(classConstraintMap.get(classUnitEntity.getId()) == null) {
                new ClassConstraintEntity(
                    classUnitMap.get(classId),
                    constraintEntity);
            }

            classConstraintMap.remove(classUnitEntity.getId());
        });

        // If there are any leftovers they must be removed
        classConstraintMap.forEach((classBDId, classConstraint) -> {
            classConstraint.getConstraintEntity().removeClassConstraint(classConstraint);
            classConstraint.getClassUnitEntity().removeClassConstraint(classConstraint);
        });
    }
}