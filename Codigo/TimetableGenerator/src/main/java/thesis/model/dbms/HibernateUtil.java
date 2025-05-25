package thesis.model.dbms;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import thesis.model.entities.*;

import java.util.Properties;

public class HibernateUtil {
    private static SessionFactory sessionFactory;

    public static void init(String dbName, String ip, String port, String user, String password) {
        try {
            Properties props = new Properties();
            props.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
            props.setProperty("hibernate.connection.url", "jdbc:postgresql://" + ip + ":" + port + "/" + dbName);
            props.setProperty("hibernate.connection.username", user);
            props.setProperty("hibernate.connection.password", password);
            props.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
            props.setProperty("hibernate.hbm2ddl.auto", "validate");
            props.setProperty("hibernate.show_sql", "true");

            Configuration cfg = new Configuration();
            cfg.addProperties(props);

            cfg.addAnnotatedClass(ClassRestriction.class);
            cfg.addAnnotatedClass(ClassRoom.class);
            cfg.addAnnotatedClass(ClassTime.class);
            cfg.addAnnotatedClass(ClassUnit.class);
            cfg.addAnnotatedClass(Config.class);
            cfg.addAnnotatedClass(Course.class);
            cfg.addAnnotatedClass(OptimizationParameters.class);
            cfg.addAnnotatedClass(Restriction.class);
            cfg.addAnnotatedClass(Room.class);
            cfg.addAnnotatedClass(RoomDistance.class);
            cfg.addAnnotatedClass(RoomUnavailability.class);
            cfg.addAnnotatedClass(ScheduledLesson.class);
            cfg.addAnnotatedClass(ScheduledLessonTeacher.class);
            cfg.addAnnotatedClass(Subpart.class);
            cfg.addAnnotatedClass(Teacher.class);
            cfg.addAnnotatedClass(TeacherClass.class);
            cfg.addAnnotatedClass(TeacherUnavailability.class);
            cfg.addAnnotatedClass(Timetable.class);
            cfg.addAnnotatedClass(TimetableConfiguration.class);

            sessionFactory = cfg.buildSessionFactory();
        } catch (Exception ex) {
            throw new RuntimeException("Errow while loading Hibernate configuration: " + ex.getMessage(), ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}
