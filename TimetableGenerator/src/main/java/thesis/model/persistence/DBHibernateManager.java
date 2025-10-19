package thesis.model.persistence;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thesis.model.domain.InMemoryRepository;
import thesis.model.domain.components.*;
import thesis.model.exceptions.DatabaseException;
import thesis.model.mapper.ModelConverter;
import thesis.model.persistence.repository.EntityRepository;
import thesis.model.persistence.repository.entities.*;
import thesis.model.persistence.repository.entities.utils.RoomEntityFactory;
import thesis.model.persistence.repository.entities.utils.TeacherEntityFactory;

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
                    EntityRepository data = new EntityRepository();
                    data.storeProgram(programEntity);

                    for (CourseEntity courseEntity : programEntity.getCourseEntitySet()) {
                        data.storeCourse(courseEntity);

                        // Get all the rooms and teachers for this problem
                        for (ConfigEntity configEntity : courseEntity.getConfigSet()) {
                            for (SubpartEntity subpartEntity : configEntity.getSubpartSet()) {
                                for (ClassUnitEntity classUnitEntity : subpartEntity.getClassUnitSet()) {
                                    // Get every room
                                    for (ClassRoomEntity classRoomEntity : classUnitEntity.getClassRoomEntitySet()) {
                                        data.storeRoom(classRoomEntity.getRoom());
                                    }

                                    // Get every teacher
                                    for (TeacherClassEntity teacherClassEntity : classUnitEntity.getTeacherClassEntitySet()) {
                                        data.storeTeacher(teacherClassEntity.getTeacherEntity());
                                    }

                                    // Get every constraint
                                    for (ClassConstraintEntity classConstraintEntity : classUnitEntity.getClassConstraintEntitySet()) {
                                        data.storeConstraintEntity(classConstraintEntity.getConstraintEntity());
                                    }
                                }
                            }
                        }
                    }

                    for (TimetableEntity timetableEntity : programEntity.getTimetableEntitySet()) {
                        data.storeTimetable(timetableEntity);
                    }

                    try {
                        allData.add(ModelConverter.convertToDomain(data));
                    } catch (Exception ignored) {
                        // Should be impossible, unless the data that causes the problem
                        // was added on the database manually
                        logger.error("An exception occurred: {}\nStacktrace: {}", ignored.getMessage(), Arrays.toString(ignored.getStackTrace()));
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

                    if (dbProgram == null) {
                        // Create a complete new instance
                        EntityRepository entityRepository = ModelConverter.convertToEntity(session, inMemoryRepository);

                        session.persist(entityRepository.getProgramEntity());
                    } else {
                        // Merge the data of the instance with the local data
                        mergeData(session, dbProgram, inMemoryRepository);
                    }
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

    private void mergeData(Session session, ProgramEntity programEntity, InMemoryRepository programData) {
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
}
