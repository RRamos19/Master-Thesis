package thesis.model.persistence;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thesis.model.domain.DataRepository;
import thesis.model.domain.InMemoryRepository;
import thesis.model.domain.components.*;
import thesis.model.domain.components.constraints.ConstraintFactory;
import thesis.model.exceptions.CheckedIllegalArgumentException;
import thesis.model.exceptions.DatabaseException;
import thesis.model.exceptions.InvalidConfigurationException;
import thesis.model.persistence.entities.*;
import thesis.model.persistence.entities.utils.RoomEntityFactory;
import thesis.model.persistence.entities.utils.TeacherEntityFactory;

import java.time.LocalDateTime;
import java.util.*;

public class DBHibernateManager implements DBManager<InMemoryRepository> {
    private static final Logger logger = LoggerFactory.getLogger(DBHibernateManager.class);

    private final SessionFactory sessionFactory;

    /**
     * Creates a connection to the database using Hibernate
     * @param dbName Name of the database
     * @param ip Ip of the system that is running the database
     * @param port Port of the system that is running the database
     * @param userName Name of the user that is connecting
     * @param password Password to authenticate the connection
     */
    public DBHibernateManager(String dbName, String ip, String port, String userName, String password) throws DatabaseException {
        try {
            sessionFactory = HibernateUtils.init(dbName, ip, port, userName, password);
        } catch (HibernateException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    private Collection<String> checkIfFetchIsNeeded(Map<String, InMemoryRepository> storedData) {
        List<String> programsToFetch = new ArrayList<>();

        try(Session session = sessionFactory.openSession()) {
            List<Object[]> programs = session.createQuery("select a.name, a.lastUpdatedAt from ProgramEntity a", Object[].class).getResultList();

            for(Object[] data : programs) {
                String programName = (String) data[0];
                LocalDateTime dataLastUpdate = (LocalDateTime) data[1];

                InMemoryRepository programData = storedData.get(programName);

                if(programData != null) {
                    LocalDateTime storedDataLastUpdate = programData.getLastUpdatedAt();

                    // If the stored data is the same or more recent there is no need to fetch
                    if(storedDataLastUpdate.equals(dataLastUpdate) ||
                        storedDataLastUpdate.isAfter(dataLastUpdate)) {
                        continue;
                    }
                }

                programsToFetch.add(programName);
            }
        }

        return programsToFetch;
    }

    /**
     * Selects all the data available in the database and stores in its respective objects
     * @return Aggregate of all the data stored in the database
     */
    public Collection<InMemoryRepository> fetchData(Map<String, InMemoryRepository> storedData) {
        Collection<InMemoryRepository> allData = new ArrayList<>();
        Collection<String> programsToFetch = checkIfFetchIsNeeded(storedData);

        try(Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            for(String programName : programsToFetch) {
                List<ProgramEntity> programs = session.createQuery("SELECT a FROM ProgramEntity a WHERE a.name = :name", ProgramEntity.class)
                        .setParameter("name", programName)
                        .getResultList();

                for (ProgramEntity programEntity : programs) {
                    try {
                        allData.add(convertToDomain(programEntity));
                    } catch (Exception e) {
                        // Should be impossible, unless the data that causes the problem
                        // was added on the database manually
                        logger.error("An exception occurred: {}\nStacktrace: {}", e.getMessage(), Arrays.toString(e.getStackTrace()));
                    }
                }
            }

            tx.commit();
        }

        return allData;
    }

    private Collection<String> checkIfStoreIsNeeded(Map<String, InMemoryRepository> storedData) {
        List<String> programsToStore = new ArrayList<>(storedData.keySet());

        try(Session session = sessionFactory.openSession()) {
            List<Object[]> programs = session.createQuery("select a.name, a.lastUpdatedAt from ProgramEntity a", Object[].class).getResultList();

            for(Object[] data : programs) {
                String programName = (String) data[0];
                LocalDateTime dataLastUpdate = (LocalDateTime) data[1];

                InMemoryRepository programData = storedData.get(programName);

                if(programData != null) {
                    LocalDateTime storedDataLastUpdate = programData.getLastUpdatedAt();

                    // If the data in the database is the same or more recent there is no need to fetch
                    if(storedDataLastUpdate.equals(dataLastUpdate) ||
                        storedDataLastUpdate.isBefore(dataLastUpdate)) {
                        programsToStore.remove(programName);
                    }
                }
            }
        }

        return programsToStore;
    }

    /**
     * Stores all data present in EntityModel into the database
     * @param storedData Collection of classes that contain all the data to be inserted
     */
    public void storeData(Map<String, InMemoryRepository> storedData) {
        Collection<String> dataToStore = checkIfStoreIsNeeded(storedData);

        try(Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            try {
                for (String programToStore : dataToStore) {
                    InMemoryRepository inMemoryRepository = storedData.get(programToStore);

                    List<ProgramEntity> storedPrograms = session.createQuery(
                                    "SELECT p FROM ProgramEntity p WHERE p.name = :name", ProgramEntity.class)
                            .setParameter("name", programToStore)
                            .getResultList();

                    // As the name of the program is unique, at most only a single result should be returned
                    ProgramEntity dbProgram = storedPrograms.isEmpty() ? null : storedPrograms.get(0);

                    // Create a new instance if one isn't stored
                    if(dbProgram == null) {
                        dbProgram = new ProgramEntity();
                        dbProgram.setName(programToStore);
                    }

                    // Creates and or merges all the data if there are instances in the database
                    createOrMergeData(session, dbProgram, inMemoryRepository);
                }
                tx.commit();

            } catch (Exception e) {
                logger.error("An exception occurred: {}\nStacktrace: {}", e.getMessage(), Arrays.toString(e.getStackTrace()));
                if (tx.getStatus().canRollback()) {
                    tx.rollback();
                }

                // Propagate the exception
                throw e;
            }
        }
    }

    private void createOrMergeData(Session session, ProgramEntity programEntity, InMemoryRepository programData) {
        TeacherEntityFactory teacherEntityFactory = new TeacherEntityFactory(session);
        RoomEntityFactory roomEntityFactory = new RoomEntityFactory(session);

        DataMerger dataMerger = new DataMerger(session, roomEntityFactory, teacherEntityFactory);

        TimetableConfiguration timetableConfiguration = programData.getTimetableConfiguration();

        programEntity.setNumberDays(timetableConfiguration.getNumDays());
        programEntity.setNumberWeeks(timetableConfiguration.getNumWeeks());
        programEntity.setSlotsPerDay(timetableConfiguration.getSlotsPerDay());

        programEntity.setRoomWeight(timetableConfiguration.getRoomWeight());
        programEntity.setTimeWeight(timetableConfiguration.getTimeWeight());
        programEntity.setDistributionWeight(timetableConfiguration.getDistribWeight());

        programEntity.setLastUpdatedAt(programData.getLastUpdatedAt());

        session.persist(programEntity);

        // Create the room instances
        programData.getRooms().forEach((room) -> {
            RoomEntity roomEntity = roomEntityFactory.getOrCreateRoom(room.getRoomId());

            room.getRoomDistances().forEach((roomId, distance) -> {
                boolean alreadyExists = false;

                for(RoomDistanceEntity roomDistanceEntity : roomEntity.getRoom1DistanceSet()) {
                    if(Objects.equals(roomDistanceEntity.getRoom2().getName(), roomId)) {
                        alreadyExists = true;
                        roomDistanceEntity.setDistance(distance);
                        break;
                    }
                }

                if(!alreadyExists) {
                    new RoomDistanceEntity(roomEntity, roomEntityFactory.getOrCreateRoom(roomId), distance);
                }
            });
        });

        // Create the teacher instances
        programData.getTeachers().forEach((teacher) -> {
            TeacherEntity teacherEntity = teacherEntityFactory.get(teacher.getId());
            if(teacherEntity == null) {
                teacherEntityFactory.create(teacher.getId(), teacher.getName());
            }
        });

        // Merge the classes
        dataMerger.mergeClasses(programEntity, programData);

        programData.getTimetableList().forEach((timetable -> {

        }));
    }

    private InMemoryRepository convertToDomain(ProgramEntity programEntity) throws CheckedIllegalArgumentException, InvalidConfigurationException {
        InMemoryRepository data = new DataRepository(programEntity.getName());

        data.setConfiguration(
                programEntity.getNumberDays(),
                programEntity.getNumberWeeks(),
                programEntity.getSlotsPerDay());

        data.setOptimizationParameters(
                programEntity.getTimeWeight(),
                programEntity.getRoomWeight(),
                programEntity.getDistributionWeight());

        data.setLastUpdatedAt(programEntity.getLastUpdatedAt());

        // Store the teachers, rooms and constraints in lists to perform the creations in the correct order
        List<TeacherEntity> teacherEntityList = new ArrayList<>();
        List<RoomEntity> roomEntityList = new ArrayList<>();
        Set<ConstraintEntity> constraintEntitySet = new HashSet<>();
        for (CourseEntity courseEntity : programEntity.getCourseEntitySet()) {
            // Get all the rooms and teachers for this problem
            for (ConfigEntity configEntity : courseEntity.getConfigSet()) {
                for (SubpartEntity subpartEntity : configEntity.getSubpartSet()) {
                    for (ClassUnitEntity classUnitEntity : subpartEntity.getClassUnitSet()) {
                        // Get every room
                        for (ClassRoomEntity classRoomEntity : classUnitEntity.getClassRoomEntitySet()) {
                            roomEntityList.add(classRoomEntity.getRoom());
                        }

                        // Get every teacher
                        for (TeacherClassEntity teacherClassEntity : classUnitEntity.getTeacherClassEntitySet()) {
                            teacherEntityList.add(teacherClassEntity.getTeacherEntity());
                        }

                        // Get every constraint
                        for (ClassConstraintEntity classConstraintEntity : classUnitEntity.getClassConstraintEntitySet()) {
                            constraintEntitySet.add(classConstraintEntity.getConstraintEntity());
                        }
                    }
                }
            }
        }

        // Add the rooms and the relations of distance and unavailabilities
        for(RoomEntity roomEntity : roomEntityList) {
            Room room = RoomFastIdFactory.createRoom(roomEntity.getName());

            for(RoomDistanceEntity roomDistanceEntity : roomEntity.getRoom1DistanceSet()) {
                room.addRoomDistance(roomDistanceEntity.getRoom2().getName(), roomDistanceEntity.getDistance());
            }

            for(TimeBlockEntity roomUnavailabilityEntity : roomEntity.getRoomUnavailabilityList()) {
                room.addUnavailability(
                        roomUnavailabilityEntity.getDays(),
                        roomUnavailabilityEntity.getWeeks(),
                        roomUnavailabilityEntity.getStartSlot(),
                        roomUnavailabilityEntity.getDuration());
            }

            data.addRoom(room);
        }

        Map<ConstraintEntity, Constraint> constraintMap = new HashMap<>();
        for(ConstraintEntity constraintEntity : constraintEntitySet) {
            Constraint constraint = ConstraintFactory.createConstraint(
                    constraintEntity.getId().getConstraintPK(),
                    constraintEntity.getConstraintTypeEntity().getName(),
                    constraintEntity.getFirst_parameter(),
                    constraintEntity.getSecond_parameter(),
                    constraintEntity.getPenalty(),
                    constraintEntity.getRequired(),
                    data.getTimetableConfiguration());

            data.addConstraint(constraint);

            constraintMap.put(constraintEntity, constraint);
        }

        // Add the teachers and their unavailabilities
        Map<TeacherEntity, Teacher> teacherMap = new HashMap<>();
        for(TeacherEntity teacherEntity : teacherEntityList) {
            Teacher teacher = new Teacher(teacherEntity.getId(), teacherEntity.getName());
            teacherMap.put(teacherEntity, teacher);

            for(TeacherUnavailabilityEntity teacherUnavailabilityEntity : teacherEntity.getTeacherUnavailabilityEntitySet()) {
                teacher.addUnavailability(
                        teacherUnavailabilityEntity.getDays(),
                        teacherUnavailabilityEntity.getWeeks(),
                        teacherUnavailabilityEntity.getStartSlot(),
                        teacherUnavailabilityEntity.getDuration());
            }

            data.addTeacher(teacher);
        }

        // Add the courses, configs, subparts, classes and respective teachers
        for(CourseEntity courseEntity : programEntity.getCourseEntitySet()) {
            Course course = new Course(courseEntity.getCourseNameEntity().getName());
            for(ConfigEntity configEntity : courseEntity.getConfigSet()) {
                Config config = new Config(configEntity.getConfigNameEntity().getName());
                for(SubpartEntity subpartEntity : configEntity.getSubpartSet()) {
                    Subpart subpart = new Subpart(subpartEntity.getSubpartNameEntity().getName());
                    for(ClassUnitEntity classUnitEntity : subpartEntity.getClassUnitSet()) {
                        ClassUnit classUnit = new ClassUnit(classUnitEntity.getClassUnitNameEntity().getName());
                        classUnit.setParentClassId(classUnit.getParentClassId());

                        for(TeacherClassEntity teacherClassEntity : classUnitEntity.getTeacherClassEntitySet()) {
                            Teacher teacher = teacherMap.get(teacherClassEntity.getTeacherEntity());
                            classUnit.addTeacher(teacher.getId());
                            teacher.addClassUnit(classUnit.getClassId());
                        }
                        for(ClassConstraintEntity classConstraintEntity : classUnitEntity.getClassConstraintEntitySet()) {
                            Constraint constraint = constraintMap.get(classConstraintEntity.getConstraintEntity());
                            classUnit.addConstraint(constraint);
                            constraint.addClassUnitId(classUnit.getClassId());
                        }
                        for(ClassRoomEntity classRoomEntity : classUnitEntity.getClassRoomEntitySet()) {
                            classUnit.addRoom(classRoomEntity.getRoom().getName(), classRoomEntity.getPenalty());
                        }
                        for(ClassTimeEntity classTimeEntity : classUnitEntity.getClassTimeEntitySet()) {
                            TimeBlockEntity timeBlockEntity = classTimeEntity.getTimeBlockEntity();
                            classUnit.addClassTime(timeBlockEntity.getDays(), timeBlockEntity.getWeeks(), timeBlockEntity.getStartSlot(), timeBlockEntity.getDuration(), classTimeEntity.getPenalty());
                        }

                        subpart.addClassUnit(classUnit);
                        data.addClassUnit(classUnit);
                    }
                    config.addSubpart(subpart);
                }
                course.addConfig(config);
            }
            data.addCourse(course);
        }

        // Add the timetables, scheduled lessons and respective teachers
        for(TimetableEntity timetableEntity : programEntity.getTimetableEntitySet()) {
            Timetable timetable = new Timetable(timetableEntity.getId(), data.getProgramName(), timetableEntity.getCreationDate());

            for(ScheduledLessonEntity scheduledLessonEntity : timetableEntity.getScheduledLessonEntityList()) {
                TimeBlockEntity timeBlockEntity = scheduledLessonEntity.getTimeBlockEntity();
                ScheduledLesson scheduledLesson = new ScheduledLesson(
                        scheduledLessonEntity.getClassUnitEntity().getClassUnitNameEntity().getName(),
                        scheduledLessonEntity.getRoomEntity().getName(),
                        timeBlockEntity.getDays(),
                        timeBlockEntity.getWeeks(),
                        timeBlockEntity.getStartSlot(),
                        timeBlockEntity.getDuration());

                for(ScheduledLessonTeacherEntity scheduledLessonTeacherEntity : scheduledLessonEntity.getScheduledLessonTeacherList()) {
                    scheduledLesson.addTeacherId(scheduledLessonTeacherEntity.getTeacher().getId());
                }

                scheduledLesson.bindModel(data);
                timetable.addScheduledLesson(scheduledLesson);
            }
            data.addTimetable(timetable);
        }

        return data;
    }
}
