package thesis.model.domain;

import thesis.model.domain.components.*;
import thesis.model.exceptions.InvalidConfigurationException;
import thesis.model.parser.XmlResult;

import java.time.LocalDateTime;
import java.util.Collection;

public interface InMemoryRepository extends XmlResult {
    // Setter and Getter of the problem's name
    String getProgramName();
    void setProgramName(String programName);
    void setLastUpdatedAt();
    void setLastUpdatedAt(LocalDateTime updateTime);
    LocalDateTime getLastUpdatedAt();

    // Configuration of the problem
    void setOptimizationParameters(short timeWeight, short roomWeight, short distribWeight);
    void setConfiguration(short numDays, int numWeeks, short slotPerDay);
    TimetableConfiguration getTimetableConfiguration();

    // Setters and Getters of the elements present in the problem
    void addRoom(Room room);
    Collection<Room> getRooms();
    Room getRoom(String roomId);

    void addTeacher(Teacher teacher);
    Teacher getTeacher(int teacherId);
    Collection<Teacher> getTeachers();

    void addCourse(Course course);
    Course getCourse(String courseId);
    Collection<Course> getCourses();

    Collection<Config> getConfigs();
    Collection<Subpart> getSubparts();

    void addClassUnit(ClassUnit classUnit);
    Collection<ClassUnit> getClassUnits();
    ClassUnit getClassUnit(String classUnitId);

    void addConstraint(Constraint constraint);
    Collection<Constraint> getConstraints();

    void addTimetable(Timetable timetable) throws InvalidConfigurationException;
    void removeTimetable(Timetable timetable);
    Collection<Timetable> getTimetableList();

    // Is used to optimize constraint verifications
    void setRoomBidirectionalDistances();

    void cleanUnusedData();

    void verifyValidity() throws InvalidConfigurationException;
}
