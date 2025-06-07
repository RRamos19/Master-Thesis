package thesis.model.repository;

import org.hibernate.Session;
import org.hibernate.Transaction;
import thesis.model.persistence.EntityModel;
import thesis.model.persistence.entities.*;

import java.util.List;

public class DBHibernateManager implements DBManager<EntityModel> {
    public EntityModel fetchData() {
        EntityModel data = new EntityModel();

        try(Session session = HibernateUtils.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            OptimizationParametersEntity optmizParams = session.createQuery("SELECT a from OptimizationParameters a", OptimizationParametersEntity.class).getResultList().get(0);
            data.storeOptimization(optmizParams);

            TimetableConfigurationEntity timetableConfig = session.createQuery("SELECT a from TimetableConfiguration a", TimetableConfigurationEntity.class).getResultList().get(0);
            data.storeConfiguration(timetableConfig);

            List<TeacherEntity> teacherEntityList = session.createQuery("SELECT a from Teacher a", TeacherEntity.class).getResultList();
            for(TeacherEntity t : teacherEntityList) {
                data.storeTeacher(t);
            }

            List<CourseEntity> courseEntityList = session.createQuery("SELECT a from Course a", CourseEntity.class).getResultList();
            for(CourseEntity c : courseEntityList) {
                data.storeCourse(c);
            }

            List<ConfigEntity> configEntityList = session.createQuery("SELECT a from Config a", ConfigEntity.class).getResultList();
            for(ConfigEntity c : configEntityList) {
                data.storeConfig(c);
            }

            List<SubpartEntity> subpartEntityList = session.createQuery("SELECT a from Subpart a", SubpartEntity.class).getResultList();
            for(SubpartEntity s : subpartEntityList) {
                data.storeSubpart(s);
            }

            List<ClassUnitEntity> classList = session.createQuery("SELECT a from ClassUnit a", ClassUnitEntity.class).getResultList();
            for(ClassUnitEntity c : classList) {
                data.storeClassUnit(c);
            }

            List<RestrictionEntity> restrictionEntityList = session.createQuery("SELECT a from Restriction a", RestrictionEntity.class).getResultList();
            for(RestrictionEntity r : restrictionEntityList) {
                data.storeRestriction(r);
            }

            List<RoomEntity> roomEntityList = session.createQuery("SELECT a from Room a", RoomEntity.class).getResultList();
            for(RoomEntity r : roomEntityList) {
                data.storeRoom(r);
            }

            List<TimetableEntity> timetableEntityList = session.createQuery("SELECT a from Timetable a", TimetableEntity.class).getResultList();
            for(TimetableEntity t : timetableEntityList) {
                data.storeTimetable(t);
            }

            tx.commit();
        }

        return data;
    }

    public void storeData(EntityModel data) {
        try(Session session = HibernateUtils.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            for(TeacherEntity t : data.getTeachers()) {
                //session.merge(t);
                TeacherEntity newT = saveOnDatabase(session, t, t.getId());
                if(!newT.equals(t)) {
                    data.storeTeacher(newT);
                }
            }

            for(RoomEntity r : data.getRooms()) {
                //session.merge(r);
                RoomEntity newR = saveOnDatabase(session, r, r.getId());
                if(!newR.equals(r)) {
                    data.storeRoom(newR);
                }
            }

            for(CourseEntity c : data.getCourses()) {
                //session.merge(c);
                CourseEntity newC = saveOnDatabase(session, c, c.getId());
                if(!newC.equals(c)) {
                    data.storeCourse(newC);
                }
            }

            for(ConfigEntity c : data.getConfigs()) {
                //session.merge(c);
                ConfigEntity newC = saveOnDatabase(session, c, c.getId());
                if(!newC.equals(c)) {
                    data.storeConfig(newC);
                }
            }

            for(SubpartEntity s : data.getSubparts()) {
                //session.merge(s);
                SubpartEntity newS = saveOnDatabase(session, s, s.getId());
                if(!newS.equals(s)) {
                    data.storeSubpart(newS);
                }
            }

            for(ClassUnitEntity c : data.getClassUnits()) {
                //session.merge(c);
                ClassUnitEntity newC = saveOnDatabase(session, c, c.getId());
                if(!newC.equals(c)) {
                    data.storeClassUnit(newC);
                }
            }

            for(TimetableEntity t : data.getTimetables()) {
                //session.merge(t);
                TimetableEntity newT = saveOnDatabase(session, t, t.getId());
                if(!newT.equals(t)) {
                    data.storeTimetable(newT);
                }
            }

            tx.commit();
        }
    }

    private <T> T saveOnDatabase(Session session, T o, Object objId) {
        // If the object isn't found then we need to persist to create the entity in the database
        if(objId == null || session.find(o.getClass(), objId) == null) {
            session.persist(o);
            return o;
        } else {
            // If the object is found we just need to merge with the database to update the values
            return session.merge(o);
        }
    }
}
