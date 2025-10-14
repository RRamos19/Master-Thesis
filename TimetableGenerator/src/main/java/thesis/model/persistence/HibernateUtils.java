package thesis.model.persistence;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import thesis.model.persistence.repository.entities.*;

import java.util.Properties;

public class HibernateUtils {
    public static SessionFactory init(String dbName, String ip, String port, String user, String password) throws HibernateException {
        Properties props = new Properties();
        props.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
        props.setProperty("hibernate.connection.url", "jdbc:postgresql://" + ip + ":" + port + "/" + dbName);
        props.setProperty("hibernate.connection.username", user);
        props.setProperty("hibernate.connection.password", password);
        props.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        props.setProperty("hibernate.hbm2ddl.auto", "none");

        //props.setProperty("hibernate.show_sql", "true");
        //props.setProperty("hibernate.format_sql", "true");
        //props.setProperty("hibernate.highlight_sql", "true");

        Configuration cfg = new Configuration();
        cfg.addProperties(props);

        cfg.addAnnotatedClass(TimeBlockEntity.class);
        cfg.addAnnotatedClass(ClassRoomEntity.class);
        cfg.addAnnotatedClass(ClassTimeEntity.class);
        cfg.addAnnotatedClass(ClassUnitEntity.class);
        cfg.addAnnotatedClass(ConfigEntity.class);
        cfg.addAnnotatedClass(ConstraintEntity.class);
        cfg.addAnnotatedClass(ConstraintTypeEntity.class);
        cfg.addAnnotatedClass(CourseEntity.class);
        cfg.addAnnotatedClass(ProgramEntity.class);
        cfg.addAnnotatedClass(RoomDistanceEntity.class);
        cfg.addAnnotatedClass(RoomEntity.class);
        cfg.addAnnotatedClass(ScheduledLessonEntity.class);
        cfg.addAnnotatedClass(ScheduledLessonTeacherEntity.class);
        cfg.addAnnotatedClass(SubpartEntity.class);
        cfg.addAnnotatedClass(TeacherEntity.class);
        cfg.addAnnotatedClass(TeacherUnavailabilityEntity.class);
        cfg.addAnnotatedClass(TimetableEntity.class);

        return cfg.buildSessionFactory();
    }
}
