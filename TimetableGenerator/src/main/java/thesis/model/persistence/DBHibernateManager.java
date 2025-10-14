package thesis.model.persistence;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import thesis.model.domain.InMemoryRepository;
import thesis.model.exceptions.DatabaseException;
import thesis.model.mapper.ModelConverter;
import thesis.model.persistence.repository.EntityRepository;
import thesis.model.persistence.repository.entities.*;

import java.util.*;

public class DBHibernateManager implements DBManager<Collection<InMemoryRepository>> {
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

    /**
     * Selects all the data available in the database and stores in its respective objects
     * @return Aggregate of all the data stored in the database
     */
    public Collection<InMemoryRepository> fetchData() {
        Collection<InMemoryRepository> allData = new ArrayList<>();

        try(Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            List<ProgramEntity> programs = session.createQuery("select a from ProgramEntity a", ProgramEntity.class).getResultList();

            for(ProgramEntity programEntity : programs) {
                EntityRepository data = new EntityRepository();
                data.storeProgram(programEntity);

                for(CourseEntity courseEntity : programEntity.getCourseEntitySet()) {
                    data.storeCourse(courseEntity);

                    // Get all the rooms and teachers for this problem
                    for(ConfigEntity configEntity: courseEntity.getConfigSet()) {
                        for(SubpartEntity subpartEntity : configEntity.getSubpartSet()) {
                            for(ClassUnitEntity classUnitEntity : subpartEntity.getClassUnitSet()) {
                                // Get every room
                                for(ClassRoomEntity classRoomEntity : classUnitEntity.getClassRoomEntitySet()) {
                                    data.storeRoom(classRoomEntity.getRoom());
                                }

                                // Get every teacher
                                for(TeacherEntity teacherEntity : classUnitEntity.getTeacherEntitySet()) {
                                    data.storeTeacher(teacherEntity);
                                }

                                // Get every constraint
                                for(ConstraintEntity constraintEntity : classUnitEntity.getConstraintEntitySet()) {
                                    data.storeConstraintEntity(constraintEntity);
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
                }
            }

            tx.commit();
        }

        return allData;
    }

    /**
     * Stores all data present in EntityModel into the database
     * @param allData Collection of classes that contain all the data to be inserted
     */
    public void storeData(Collection<InMemoryRepository> allData) {
        List<EntityRepository> convertedData = new ArrayList<>();
        for(InMemoryRepository data : allData) {
            convertedData.add(ModelConverter.convertToEntity(sessionFactory, data));
        }

        // TODO: Fix method
        try(Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            try {
                for (EntityRepository data : convertedData) {
                    ProgramEntity program = data.getProgramEntity();

                    List<ProgramEntity> storedPrograms = session.createQuery(
                                    "SELECT p FROM ProgramEntity p WHERE p.name = :name", ProgramEntity.class)
                            .setParameter("name", program.getName())
                            .getResultList();

                    // As the name of the program is unique, at most only a single result should be returned
                    ProgramEntity storedProgram = storedPrograms.isEmpty() ? null : storedPrograms.get(0);

                    ProgramEntity newProgram;
                    if (storedProgram != null) {
                        // Set the same id to overwrite the stored values
                        program.setId(storedProgram.getId());

                        newProgram = session.merge(program);
                        data.storeProgram(newProgram);
                    } else {
                        session.persist(program);
                    }
                }
                tx.commit();

            } catch (Exception e) {
                if (tx.getStatus().canRollback()) {
                    tx.rollback();
                }

                // Propagate the exception
                throw e;
            }
        }
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }


//    /**
//     * Saves or updates the data provided on the database
//     * @param session Session used to connect to the database
//     * @param o Object that is to be stored
//     * @param objId Primary key of the object provided
//     * @return Either the same object provided in the case of persist or the return of the merge
//     * @param <T> Type of the object provided
//     */
//    private <T> T saveOnDatabase(Session session, T o, Object objId) {
//        if(objId == null || session.find(o.getClass(), objId) == null) {
//            // If the object isn't found in the DB then a persist
//            // is needed to create the entity in the database
//            session.persist(o);
//            return o;
//        } else {
//            // If the object is found we just need to merge with the database to update the values
//            return session.merge(o);
//        }
//    }
}
