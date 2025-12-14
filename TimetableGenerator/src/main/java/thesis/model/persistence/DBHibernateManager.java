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
import thesis.model.persistence.entities.utils.TimeBlockEntityFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
            Transaction tx = session.beginTransaction();

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

            tx.commit();
        }

        return programsToFetch;
    }

    private void syncTimetables(InMemoryRepository repo, Session session) throws CheckedIllegalArgumentException, InvalidConfigurationException {
        List<UUID> existingIds = repo.getTimetableList().stream()
                .map(Timetable::getTimetableId)
                .collect(Collectors.toList());

        Collection<Timetable> newOnes =
                fetchSolutions(repo.getProgramName(), existingIds, session);

        for(Timetable timetable : newOnes) {
            repo.addTimetable(timetable);
        }
    }

    private void syncRooms(InMemoryRepository repo, List<RoomEntity> roomEntities) throws CheckedIllegalArgumentException {
        for (RoomEntity entity : roomEntities) {
            Room room = repo.getRoom(entity.getName());

            if (room == null) continue;

            room.clearRoomDistances();
            for (RoomDistanceEntity roomDistanceEntity : entity.getRoom1DistanceSet()) {
                room.addRoomDistance(roomDistanceEntity.getRoom2().getName(), roomDistanceEntity.getDistance());
            }
            room.optimizeRoomDistances();

            room.clearRoomUnavailabilities();
            for (TimeBlockEntity unavailabilityEntity : entity.getRoomUnavailabilityList()) {
                room.addUnavailability(TimeFactory.create(
                    unavailabilityEntity.getDays(),
                    unavailabilityEntity.getWeeks(),
                    unavailabilityEntity.getStartSlot(),
                    unavailabilityEntity.getDuration()));
            }
        }
    }

    private void syncTeachers(InMemoryRepository repo, List<TeacherEntity> teacherEntities) throws CheckedIllegalArgumentException {
        for (TeacherEntity entity : teacherEntities) {
            Teacher teacher = repo.getTeacher(entity.getId());
            if (teacher == null) continue;

            teacher.setName(entity.getName());
            teacher.clearTeacherUnavailabilities();

            for(TeacherUnavailabilityEntity teacherUnavailabilityEntity : entity.getTeacherUnavailabilityEntitySet()) {
                teacher.addUnavailability(teacherUnavailabilityEntity.getDays(),
                    teacherUnavailabilityEntity.getWeeks(),
                    teacherUnavailabilityEntity.getStartSlot(),
                    teacherUnavailabilityEntity.getDuration());
            }
        }
    }

    /**
     * Selects all the data available in the database and stores in its respective objects
     * @return Aggregate of all the data stored in the database
     */
    public Collection<InMemoryRepository> fetchData(Map<String, InMemoryRepository> storedData) {
        Collection<InMemoryRepository> fetchedData = new ArrayList<>();
        Collection<String> programsToFetch = checkIfFetchIsNeeded(storedData);

        try(Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            // Only fetch the problems that are more recent than the local ones
            for(String programName : programsToFetch) {
                List<ProgramEntity> programs = session.createQuery("SELECT a FROM ProgramEntity a WHERE a.name = :name", ProgramEntity.class)
                    .setParameter("name", programName)
                    .getResultList();

                if(programs.isEmpty()) continue;

                // There is only one program per name
                ProgramEntity program = programs.get(0);

                try {
                    InMemoryRepository repository = convertToDomain(program);

                    fetchedData.add(repository);
                } catch (Exception e) {
                    // Should be impossible, unless the data that causes the problem
                    // was added on the database manually
                    logger.error("An exception occurred while fetching the data", e);
                }
            }

            //TODO: fix this workaround
            HashMap<String, InMemoryRepository> newData = new HashMap<>(storedData);
            for(InMemoryRepository repo : fetchedData) {
                newData.put(repo.getProgramName(), repo);
            }
            // Remove the newly fetched objects
            newData.values().removeIf(repo ->
                    programsToFetch.contains(repo.getProgramName()));

            // Get the data that should be equal between programs
            if(!newData.isEmpty()) {
                List<RoomEntity> roomEntities = session.createQuery("SELECT a FROM RoomEntity a", RoomEntity.class)
                        .getResultList();
                List<TeacherEntity> teacherEntities = session.createQuery("SELECT a FROM TeacherEntity a", TeacherEntity.class)
                        .getResultList();

                // Get all the timetables that are not in local (only fetches the timetables whose ids are not found in local)
                // Also update the rooms and teachers as they should be equal between problems
                for (InMemoryRepository repository : newData.values()) {
                    try {
                        syncTimetables(repository, session);
                        syncRooms(repository, roomEntities);
                        syncTeachers(repository, teacherEntities);
                    } catch (Exception e) {
                        // Should be impossible, unless the data that causes the problem
                        // was added on the database manually
                        logger.error("An exception occurred while fetching the solutions", e);
                    }
                }
            }

            tx.commit();
        }

        return fetchedData;
    }

    private Collection<Timetable> fetchSolutions(String programName, Collection<UUID> storedTimetables, Session session) throws CheckedIllegalArgumentException {
        Collection<Timetable> timetableList = new ArrayList<>();

        List<TimetableEntity> timetableEntityList = session
            .createQuery("SELECT a FROM TimetableEntity a WHERE a.programEntity.name = :timetableProgramName", TimetableEntity.class)
            .setParameter("timetableProgramName", programName)
            .getResultList();

        for(TimetableEntity timetableEntity : timetableEntityList) {
            if(storedTimetables != null && storedTimetables.contains(timetableEntity.getId())) continue;

            Timetable timetable = new Timetable(timetableEntity.getId(), timetableEntity.getProgramEntity().getName(), timetableEntity.getCreationDate(), timetableEntity.getRuntime());

            for(ScheduledLessonEntity scheduledLessonEntity : timetableEntity.getScheduledLessonEntityList()) {
                ScheduledLesson scheduledLesson = new ScheduledLesson(
                    scheduledLessonEntity.getClassUnitEntity().getClassUnitNameEntity().getName(),
                    scheduledLessonEntity.getRoomEntity().getName(),
                    scheduledLessonEntity.getTimeBlockEntity().getDays(),
                    scheduledLessonEntity.getTimeBlockEntity().getWeeks(),
                    scheduledLessonEntity.getTimeBlockEntity().getStartSlot(),
                    scheduledLessonEntity.getTimeBlockEntity().getDuration());

                timetable.addScheduledLesson(scheduledLesson);
            }

            timetableList.add(timetable);
        }

        return timetableList;
    }

    private void storeSolutions(InMemoryRepository repository) {
        try(Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            List<ProgramEntity> storedProgram = session
                .createQuery("SELECT p FROM ProgramEntity p WHERE p.name = :name", ProgramEntity.class)
                .setParameter("name", repository.getProgramName())
                .getResultList();

            if(storedProgram.isEmpty()) return;

            ProgramEntity programEntity = storedProgram.get(0);

            RoomEntityFactory roomEntityFactory = new RoomEntityFactory(session);
            TimeBlockEntityFactory timeBlockEntityFactory = new TimeBlockEntityFactory(session);

            Map<String, ClassUnitEntity> classUnitEntityMap = new HashMap<>(); // ClassId -> ClassUnit

            // Get all the classUnits to make the creation of timetables easier
            for(CourseEntity courseEntity : programEntity.getCourseEntitySet()) {
                for(ConfigEntity configEntity : courseEntity.getConfigSet()) {
                    for(SubpartEntity subpartEntity : configEntity.getSubpartSet()) {
                        for(ClassUnitEntity classUnitEntity : subpartEntity.getClassUnitSet()) {
                            classUnitEntityMap.put(classUnitEntity.getClassUnitNameEntity().getName(), classUnitEntity);
                        }
                    }
                }
            }

            for(Timetable timetable : repository.getTimetableList()) {
                boolean storeTimetable = true;
                for(TimetableEntity timetableEntity : programEntity.getTimetableEntitySet()) {
                    if (timetableEntity.getId().equals(timetable.getTimetableId())) {
                        storeTimetable = false;
                        break;
                    }
                }

                if(storeTimetable) {
                    TimetableEntity timetableEntity = new TimetableEntity(
                        timetable.getTimetableId(),
                        programEntity,
                        timetable.getDateOfCreation(),
                        timetable.getRuntime());

                    session.persist(timetableEntity);

                    for(ScheduledLesson scheduledLesson : timetable.getScheduledLessonList()) {
                        Time lessonTime = scheduledLesson.getScheduledTime();
                        RoomEntity roomEntity = scheduledLesson.getRoomId() != null ? roomEntityFactory.getOrCreateRoom(scheduledLesson.getRoomId()) : null;

                        ScheduledLessonEntity scheduledLessonEntity = new ScheduledLessonEntity(
                            timetableEntity,
                            classUnitEntityMap.get(scheduledLesson.getClassId()),
                            roomEntity,
                            timeBlockEntityFactory.getOrCreate(lessonTime.getStartSlot(), lessonTime.getLength(), lessonTime.getDays(), lessonTime.getWeeks()));

                        session.persist(scheduledLessonEntity);
                    }
                }
            }

            tx.commit();
        }
    }

    private Collection<String> checkIfStoreIsNeeded(Map<String, InMemoryRepository> storedData) {
        List<String> programsToStore = new ArrayList<>(storedData.keySet());

        try(Session session = sessionFactory.openSession()) {
            List<Object[]> programs = session.createQuery("SELECT a.name, a.lastUpdatedAt FROM ProgramEntity a", Object[].class).getResultList();

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

                    List<ProgramEntity> storedPrograms = session
                            .createQuery("SELECT p FROM ProgramEntity p WHERE p.name = :name", ProgramEntity.class)
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

                // Try to store all the solutions
                for(InMemoryRepository repository : storedData.values()) {
                    storeSolutions(repository);
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
        TimeBlockEntityFactory timeBlockEntityFactory = new TimeBlockEntityFactory(session);

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
                teacherEntity = teacherEntityFactory.create(teacher.getId(), teacher.getName());
            }
            Set<TeacherUnavailabilityEntity> teacherUnavailabilities = teacherEntity.getTeacherUnavailabilityEntitySet();

            // Removes unavailabilities not found in this program
            teacherUnavailabilities.removeIf(unavailabilityEntity ->
                    teacher.getTeacherUnavailabilities().stream().noneMatch(unavailability ->
                        unavailabilityEntity.getDays() == unavailability.getDays() &&
                        unavailabilityEntity.getWeeks() == unavailability.getWeeks() &&
                        unavailabilityEntity.getStartSlot() == unavailability.getStartSlot() &&
                        unavailabilityEntity.getDuration() == unavailability.getLength()
            ));

            // Create the remaining unavailabilities
            TeacherEntity finalTeacherEntity = teacherEntity;
            teacher.getTeacherUnavailabilities().stream()
                .filter(unavailability ->
                        teacherUnavailabilities.stream().noneMatch(unavailabilityEntity ->
                            unavailabilityEntity.getDays() == unavailability.getDays() &&
                            unavailabilityEntity.getWeeks() == unavailability.getWeeks() &&
                            unavailabilityEntity.getStartSlot() == unavailability.getStartSlot() &&
                            unavailabilityEntity.getDuration() == unavailability.getLength())
                ).forEach(unavailability -> {
                    TimeBlockEntity unavailableTime = timeBlockEntityFactory.getOrCreate(unavailability.getStartSlot(), unavailability.getLength(), unavailability.getDays(), unavailability.getWeeks());
                    TeacherUnavailabilityEntity teacherUnavailabilityEntity = new TeacherUnavailabilityEntity(finalTeacherEntity, unavailableTime);
                    session.persist(teacherUnavailabilityEntity);
                });
        });

        // Merge the classes
        dataMerger.mergeClasses(programEntity, programData);

        // Merge the solutions
        dataMerger.mergeSolutions(programEntity, programData);
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

        // Get all the constraints
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
            Timetable timetable = new Timetable(timetableEntity.getId(), data.getProgramName(), timetableEntity.getCreationDate(), timetableEntity.getRuntime());

            for(ScheduledLessonEntity scheduledLessonEntity : timetableEntity.getScheduledLessonEntityList()) {
                TimeBlockEntity timeBlockEntity = scheduledLessonEntity.getTimeBlockEntity();
                RoomEntity roomEntity = scheduledLessonEntity.getRoomEntity();
                ScheduledLesson scheduledLesson = new ScheduledLesson(
                    scheduledLessonEntity.getClassUnitEntity().getClassUnitNameEntity().getName(),
                    roomEntity != null ? roomEntity.getName() : null,
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

    @Override
    public void removeTimetable(String programName, UUID timetableId) {
        try(Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            List<ProgramEntity> programs = session
                    .createQuery("SELECT a FROM ProgramEntity a WHERE a.name = :name", ProgramEntity.class)
                    .setParameter("name", programName)
                    .getResultList();

            if(programs.isEmpty()) {
                tx.rollback();
                return;
            }

            // There is only one program per name
            ProgramEntity program = programs.get(0);

            for(TimetableEntity timetableEntity : program.getTimetableEntitySet()) {
                if(Objects.equals(timetableEntity.getId(), timetableId)) {
                    program.getTimetableEntitySet().remove(timetableEntity);
                    break;
                }
            }

            tx.commit();
        }
    }

    @Override
    public void removeProgram(String programName) {
        try(Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            List<ProgramEntity> programs = session
                .createQuery("SELECT a FROM ProgramEntity a WHERE a.name = :name", ProgramEntity.class)
                .setParameter("name", programName)
                .getResultList();

            if(programs.isEmpty()) {
                tx.rollback();
                return;
            }

            // There is only one program per name
            ProgramEntity program = programs.get(0);

            session.remove(program);

            tx.commit();
        }
    }
}
