package thesis.model.persistence;

import org.hibernate.Session;
import thesis.model.domain.InMemoryRepository;
import thesis.model.domain.components.*;
import thesis.model.persistence.repository.entities.*;
import thesis.model.persistence.repository.entities.utils.ConstraintTypeEntityFactory;
import thesis.model.persistence.repository.entities.utils.RoomEntityFactory;
import thesis.model.persistence.repository.entities.utils.TeacherEntityFactory;
import thesis.model.persistence.repository.entities.utils.TimeBlockEntityFactory;

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

    private final Map<String, List<Object>> classUnitMap = new HashMap<>();
    private final Set<Constraint> constraintSet = new HashSet<>();

    public DataMerger(Session session, RoomEntityFactory roomEntityFactory, TeacherEntityFactory teacherEntityFactory) {
        this.session = session;
        this.constraintTypeEntityFactory = new ConstraintTypeEntityFactory(session);
        this.timeBlockEntityFactory = new TimeBlockEntityFactory(session);
        this.roomEntityFactory = roomEntityFactory;
        this.teacherEntityFactory = teacherEntityFactory;
    }

    public void mergeClasses(ProgramEntity programEntity, InMemoryRepository inMemoryRepository) {
        mergeCollections(
            inMemoryRepository.getCourses(),
            programEntity.getCourseEntitySet(),
            Course::getCourseId,
            CourseEntity::getName,
            this::mergeCourses,
            (course) -> createCourseEntity(programEntity, course),
            (courseEntity) -> programEntity.getCourseEntitySet().remove(courseEntity)
        );

        // After every class is created the relations of parent class and constraints must be created
        classUnitMap.values().forEach((clsObjects) -> {
            ClassUnit classUnit = (ClassUnit) clsObjects.get(0);
            ClassUnitEntity classUnitEntity = (ClassUnitEntity) clsObjects.get(1);

            String parentClass = classUnit.getParentClassId();
            if(parentClass != null) {
                classUnitEntity.setParentClass((ClassUnitEntity) classUnitMap.get(parentClass).get(1));
            }
        });

        // Create all the constraints again
        constraintSet.forEach((constraint) -> {
            ConstraintTypeEntity constraintTypeEntity = constraintTypeEntityFactory.getConstraintType(constraint.getType());

            ConstraintEntity constraintEntity = new ConstraintEntity(
                constraintTypeEntity,
                constraint.getFirstParameter(),
                constraint.getSecondParameter(),
                constraint.getPenalty(),
                constraint.getRequired());

            constraintEntity = session.merge(constraintEntity);

            ConstraintEntity finalConstraintEntity = constraintEntity;
            constraint.getClassUnitIdList().forEach((classId) -> {
                List<Object> classObjects = classUnitMap.get(classId);
                ClassUnitEntity classUnitEntity = (ClassUnitEntity) classObjects.get(1);

                new ClassConstraintEntity(classUnitEntity, finalConstraintEntity);
            });
        });
    }

    private static <T, E> void mergeCollections(
            Collection<T> source,
            Collection<E> target,
            Function<T, String> sourceKey,
            Function<E, String> targetKey,
            BiConsumer<T, E> mergeExisting,
            Consumer<T> createNew,
            Consumer<E> removeOrphan) {

        // More efficient for searching matches in keys
        Map<String, E> targetMap = target.stream()
                .collect(Collectors.toMap(targetKey, e -> e));

        // Search each value in source to add to the database
        for (T src : source) {
            String key = sourceKey.apply(src);
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
        CourseEntity courseEntity = new CourseEntity(programEntity, course.getCourseId());
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
            ConfigEntity::getName,
            this::mergeConfigs,
            (config) -> createConfigEntity(courseEntity, config),
            (configEntity -> courseEntity.getConfigSet().remove(configEntity))
        );
    }

    private void createConfigEntity(CourseEntity courseEntity, Config config) {
        ConfigEntity configEntity = new ConfigEntity(courseEntity, config.getConfigId());
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
            SubpartEntity::getName,
            this::mergeSubparts,
            (subpart) -> createSubpartEntity(configEntity, subpart),
            (subpartEntity -> configEntity.getSubpartSet().remove(subpartEntity))
        );
    }

    private void createSubpartEntity(ConfigEntity configEntity, Subpart subpart) {
        SubpartEntity subpartEntity = new SubpartEntity(configEntity, subpart.getSubpartId());
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
            ClassUnitEntity::getName,
            this::mergeClassUnits,
            (classUnit) -> createClassUnitEntity(subpartEntity, classUnit),
            (classUnitEntity -> subpartEntity.getClassUnitSet().remove(classUnitEntity))
        );
    }

    private void createClassUnitEntity(SubpartEntity subpartEntity, ClassUnit classUnit) {
        ClassUnitEntity classUnitEntity = new ClassUnitEntity(subpartEntity, classUnit.getClassId());
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

        classUnitMap.put(classUnit.getClassId(), List.of(classUnit, classUnitEntity));
        constraintSet.addAll(classUnit.getConstraintList());
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
                .collect(Collectors.toMap((e) -> e.getTeacherEntity().getId(), e -> e));
        Map<String, ClassRoomEntity> classRoomMap = classUnitEntity.getClassRoomEntitySet().stream()
                .collect(Collectors.toMap((classRoomEntity -> classRoomEntity.getRoom().getName()), e -> e));
        Map<List<Object>, ClassTimeEntity> classTimeMap = classUnitEntity.getClassTimeEntitySet().stream()
                .collect(Collectors.toMap((e) -> createTimeBlockId(e.getTimeBlockEntity()), e -> e));

        // Clear every constraint (they will be created again after merging everything)
        classUnitEntity.getClassConstraintEntitySet().forEach((classConstraint) -> {
            ConstraintEntity constraintEntity = classConstraint.getConstraintEntity();
            constraintEntity.getClassConstraintEntityList().clear();
            session.remove(constraintEntity);
        });
        classUnitEntity.getClassConstraintEntitySet().clear();

        classUnit.getClassTimePenalties().forEach((time, penalty) -> {
            List<Object> timeId = createTimeId(time);
            boolean alreadyExists = false;
            for(ClassTimeEntity classTimeEntity : classUnitEntity.getClassTimeEntitySet()) {
                List<Object> timeBlockId = createTimeBlockId(classTimeEntity.getTimeBlockEntity());
                if(Objects.equals(timeBlockId, timeId)) {
                    alreadyExists = true;
                    classTimeMap.remove(timeBlockId);

                    // Update penalty
                    if(classTimeEntity.getPenalty() != penalty) {
                        classTimeEntity.setPenalty(penalty);
                    }

                    break;
                }
            };

            if(!alreadyExists) {
                new ClassTimeEntity(
                    classUnitEntity,
                    timeBlockEntityFactory.getOrCreate(time.getStartSlot(), time.getLength(), time.getDays(), time.getWeeks()),
                    penalty);
            }

            // Remove the leftovers
            classTimeMap.values().forEach((classTime) -> {
                classTime.getClassUnit().getClassTimeEntitySet().remove(classTime);
            });
        });

        classUnit.getClassRoomPenalties().forEach((roomId, penalty) -> {
            boolean alreadyExists = false;
            for(ClassRoomEntity classRoomEntity : classUnitEntity.getClassRoomEntitySet()) {
                if(Objects.equals(classRoomEntity.getRoom().getName(), roomId)) {
                    alreadyExists = true;
                    classRoomMap.remove(roomId);
                    break;
                }
            }

            if(!alreadyExists) {
                RoomEntity roomEntity = roomEntityFactory.getOrCreateRoom(roomId);
                new ClassRoomEntity(classUnitEntity, roomEntity, penalty);
            }

            classRoomMap.values().forEach((classRoom) -> {
                classRoom.getClassUnit().getClassRoomEntitySet().remove(classRoom);
            });
        });

        classUnit.getTeacherIdList().forEach((teacherId) -> {
            boolean alreadyExists = false;
            for(TeacherClassEntity teacherClassEntity : classUnitEntity.getTeacherClassEntitySet()) {
                TeacherEntity teacherEntity = teacherClassEntity.getTeacherEntity();
                if(Objects.equals(teacherId, teacherEntity.getId())) {
                    alreadyExists = true;
                    teacherMap.remove(teacherId);
                    break;
                }
            }

            if(!alreadyExists) {
                TeacherEntity teacherEntity = teacherEntityFactory.get(teacherId);
                new TeacherClassEntity(classUnitEntity, teacherEntity);
            }

            teacherMap.values().forEach((teacherEntity -> {
                classUnitEntity.getTeacherClassEntitySet().remove(teacherEntity);
            }));
        });

        classUnitMap.put(classUnit.getClassId(), List.of(classUnit, classUnitEntity));
        constraintSet.addAll(classUnit.getConstraintList());
    }
}
