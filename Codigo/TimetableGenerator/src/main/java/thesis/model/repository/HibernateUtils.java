package thesis.model.repository;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import thesis.model.persistence.entities.*;

import java.util.Properties;

public class HibernateUtils {
    private static SessionFactory sessionFactory;

    public static void init(String dbName, String ip, String port, String user, String password) {
        try {
            Properties props = new Properties();
            props.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
            props.setProperty("hibernate.connection.url", "jdbc:postgresql://" + ip + ":" + port + "/" + dbName);
            props.setProperty("hibernate.connection.username", user);
            props.setProperty("hibernate.connection.password", password);
            //props.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
            props.setProperty("hibernate.hbm2ddl.auto", "validate");
            props.setProperty("hibernate.show_sql", "false");

            Configuration cfg = new Configuration();
            cfg.addProperties(props);

            cfg.addAnnotatedClass(ClassConstraintEntity.class);
            cfg.addAnnotatedClass(ClassRoomEntity.class);
            cfg.addAnnotatedClass(ClassTimeEntity.class);
            cfg.addAnnotatedClass(ClassUnitEntity.class);
            cfg.addAnnotatedClass(ConfigEntity.class);
            cfg.addAnnotatedClass(ConfigurationEntity.class);
            cfg.addAnnotatedClass(ConstraintEntity.class);
            cfg.addAnnotatedClass(ConstraintTypeEntity.class);
            cfg.addAnnotatedClass(CourseEntity.class);
            cfg.addAnnotatedClass(OptimizationParametersEntity.class);
            cfg.addAnnotatedClass(ProgramEntity.class);
            cfg.addAnnotatedClass(RoomDistanceEntity.class);
            cfg.addAnnotatedClass(RoomEntity.class);
            cfg.addAnnotatedClass(RoomUnavailabilityEntity.class);
            cfg.addAnnotatedClass(ScheduledLessonEntity.class);
            cfg.addAnnotatedClass(ScheduledLessonTeacherEntity.class);
            cfg.addAnnotatedClass(SubpartEntity.class);
            cfg.addAnnotatedClass(TeacherEntity.class);
            cfg.addAnnotatedClass(TeacherUnavailabilityEntity.class);
            cfg.addAnnotatedClass(TimetableEntity.class);

            sessionFactory = cfg.buildSessionFactory();
        } catch (Exception ex) {
            throw new RuntimeException("Error while loading Hibernate configuration: " + ex.getMessage(), ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}
