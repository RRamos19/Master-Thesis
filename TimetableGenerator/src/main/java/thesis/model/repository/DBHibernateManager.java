package thesis.model.repository;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.dialect.Database;
import org.hibernate.query.Query;
import thesis.model.exceptions.DatabaseException;
import thesis.model.persistence.EntityRepository;
import thesis.model.persistence.entities.*;
import thesis.model.persistence.entities.embeddableIds.TeacherClassPK;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DBHibernateManager implements DBManager<Collection<EntityRepository>> {

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
            HibernateUtils.init(dbName, ip, port, userName, password);
        } catch (HibernateException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    /**
     * Selects all of the data available in the database and stores in its respective objects
     * @return Aggregate of all the data stored in the database
     */
    public Collection<EntityRepository> fetchData() {
        Collection<EntityRepository> allData = new ArrayList<>();

        try(Session session = HibernateUtils.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            List<ProgramEntity> programs = session.createQuery("select a from ProgramEntity a", ProgramEntity.class).getResultList();

            for(ProgramEntity programEntity : programs) {
                EntityRepository data = new EntityRepository();
                data.storeProgram(programEntity);

                List<TeacherEntity> teacherList = new ArrayList<>();
                List<RoomEntity> roomList = new ArrayList<>();

                Query<CourseEntity> courseEntityQuery = session.createQuery("select a from CourseEntity a where programEntity = :programName", CourseEntity.class);
                courseEntityQuery.setParameter("programName", programEntity);

                List<CourseEntity> courseEntityList = courseEntityQuery.getResultList();
                for(CourseEntity courseEntity : courseEntityList) {
                    data.storeCourse(courseEntity);

                    // Get all the rooms and teachers for this problem
                    for(ConfigEntity configEntity: courseEntity.getConfigList()) {
                        for(SubpartEntity subpartEntity : configEntity.getSubpartList()) {
                            for(ClassUnitEntity classUnitEntity : subpartEntity.getClassUnits()) {
                                // Get every room for this class
                                for(ClassRoomEntity classRoomEntity : classUnitEntity.getClassRoomEntityList()) {
                                    roomList.add(classRoomEntity.getRoom());
                                }

                                // Get every teacher for this class
                                teacherList.addAll(classUnitEntity.getTeacherEntityClassList());
                            }
                        }
                    }
                }

                for(TeacherEntity teacherEntity : teacherList) {
                    data.storeTeacher(teacherEntity);
                }

                for(RoomEntity roomEntity: roomList) {
                    data.storeRoom(roomEntity);
                }

                Query<TimetableEntity> timetableEntityQuery = session.createQuery("select a from TimetableEntity a where programEntity = :programName", TimetableEntity.class);
                timetableEntityQuery.setParameter("programName", programEntity);

                List<TimetableEntity> timetableEntityList = timetableEntityQuery.getResultList();
                for (TimetableEntity t : timetableEntityList) {
                    data.storeTimetable(t);
                }

                allData.add(data);
            }

            tx.commit();
        }

        return allData;
    }

    /**
     * Stores all data present in EntityModel into the database
     * @param allData Collection of classes that contain all of the data to be inserted
     */
    public void storeData(Collection<EntityRepository> allData) {
        // TODO: Fix method
        try(Session session = HibernateUtils.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            for(EntityRepository data : allData) {
                ProgramEntity program = data.getProgramEntity();
                ProgramEntity newProgram = saveOnDatabase(session, program, program.getName());
                data.storeProgram(newProgram);

//                for (TeacherEntity t : data.getTeachers()) {
//                    //session.merge(t);
//                    TeacherEntity newT = saveOnDatabase(session, t, t.getId());
//                    if (!newT.equals(t)) {
//                        data.storeTeacher(newT);
//                    }
//                }
//
//                for (RoomEntity r : data.getRooms()) {
//                    //session.merge(r);
//                    RoomEntity newR = saveOnDatabase(session, r, r.getId());
//                    if (!newR.equals(r)) {
//                        data.storeRoom(newR);
//                    }
//                }

                for (CourseEntity c : data.getCourses()) {
                    //session.merge(c);
                    CourseEntity newC = saveOnDatabase(session, c, c.getId());
                    if (!newC.equals(c)) {
                        data.storeCourse(newC);
                    }
                }

//                for (ConfigEntity c : data.getConfigs()) {
//                    //session.merge(c);
//                    ConfigEntity newC = saveOnDatabase(session, c, c.getId());
//                    if (!newC.equals(c)) {
//                        data.storeConfig(newC);
//                    }
//                }
//
//                for (SubpartEntity s : data.getSubparts()) {
//                    //session.merge(s);
//                    SubpartEntity newS = saveOnDatabase(session, s, s.getId());
//                    if (!newS.equals(s)) {
//                        data.storeSubpart(newS);
//                    }
//                }
//
//                for (ClassUnitEntity c : data.getClassUnits()) {
//                    //session.merge(c);
//                    ClassUnitEntity newC = saveOnDatabase(session, c, c.getId());
//                    if (!newC.equals(c)) {
//                        data.storeClassUnit(newC);
//                    }
//                }

                for (TimetableEntity t : data.getTimetables()) {
                    //session.merge(t);
                    TimetableEntity newT = saveOnDatabase(session, t, t.getId());
                    if (!newT.equals(t)) {
                        data.storeTimetable(newT);
                    }
                }
            }

            tx.commit();
        }
    }


    /**
     * Saves or updates the data provided on the database
     * @param session Session used to connect to the database
     * @param o Object that is to be stored
     * @param objId Primary key of the object provided
     * @return Either the same object provided in the case of persist or the return of the merge
     * @param <T> Type of the object provided
     */
    private <T> T saveOnDatabase(Session session, T o, Object objId) {
        if(objId == null || session.find(o.getClass(), objId) == null) {
            // If the object isn't found in the DB then a persist
            // is needed to create the entity in the database
            session.persist(o);
            return o;
        } else {
            // If the object is found we just need to merge with the database to update the values
            return session.merge(o);
        }
    }
}
