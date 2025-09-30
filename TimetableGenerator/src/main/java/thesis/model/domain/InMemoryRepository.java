package thesis.model.domain;

import thesis.model.domain.components.*;
import thesis.model.exceptions.InvalidConfigurationException;
import thesis.model.parser.XmlResult;

import java.util.List;

public interface InMemoryRepository extends XmlResult {
    // Setter and Getter of the problem's name
    String getProgramName();
    void setProgramName(String programName);

    // Configuration of the problem
    void setOptimizationParameters(short timeWeight, short roomWeight, short distribWeight);
    void setConfiguration(short numDays, int numWeeks, short slotPerDay);
    TimetableConfiguration getTimetableConfiguration();

    // Setters and Getters of the elements present in the problem
    void addRoom(Room room);
    List<Room> getRooms();
    Room getRoom(String roomId);

    void addTeacher(Teacher teacher);
    Teacher getTeacher(int teacherId);
    List<Teacher> getTeachers();

    void addCourse(Course course);
    Course getCourse(String courseId);
    List<Course> getCourses();

    List<Config> getConfigs();
    List<Subpart> getSubparts();

    void addClassUnit(ClassUnit classUnit);
    List<ClassUnit> getClassUnits();
    ClassUnit getClassUnit(String classUnitId);

    void addConstraint(Constraint constraint);
    List<Constraint> getConstraints();

    void addTimetable(Timetable timetable) throws InvalidConfigurationException;
    void removeTimetable(Timetable timetable);
    List<Timetable> getTimetableList();

    void merge(InMemoryRepository other);

    void verifyValidity() throws InvalidConfigurationException;
}
