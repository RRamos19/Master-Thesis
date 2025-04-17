package thesis.structures;

import java.util.ArrayList;
import java.util.HashMap;

public class StructuredTimetablingData {
    // Number of days, slots per day and weeks of the timetable
    private int nrDays = 0, slotsPerDay = 0, nrWeeks = 0;

    // Optimization data
    private int timeWeight = 0, roomWeight = 0, distributionWeight = 0;

    // Storage of the data present in the ITC Format and the Database
    private final HashMap<String, Course> courses = new HashMap<>();              // CourseId: Course
    private final HashMap<Integer, Teacher> teachers = new HashMap<>();           // TeacherId: Teacher
    private final HashMap<String, Timetable> timetables = new HashMap<>();        // TimetableId: Timetable
    private final HashMap<String, Room> rooms = new HashMap<>();                  // RoomId: Room
    private final ArrayList<Distribution> distributions = new ArrayList<>();

    public void storeTimetableConfiguration(int nrDays, int slotsPerDay, int nrWeeks) {
        this.nrDays = nrDays;
        this.slotsPerDay = slotsPerDay;
        this.nrWeeks = nrWeeks;
    }

    public void storeOptimization(int timeWeight, int roomWeight, int distributionWeight) {
        this.timeWeight = timeWeight;
        this.roomWeight = roomWeight;
        this.distributionWeight = distributionWeight;
    }

    public void storeCourse(Course course) {
        courses.put(course.getId(), course);
    }

    public Course getCourse(String courseId) {
        return courses.get(courseId);
    }

    public void storeTeacher(Teacher teacher) {
        teachers.put(teacher.getId(), teacher);
    }

    public Teacher getTeacher(int teacherId) {
        return teachers.get(teacherId);
    }

    public void storeDistribution(Distribution distribution) {
        distributions.add(distribution);
    }

    public void storeTimetable(Timetable timetable) {
        timetables.put(timetable.getId(), timetable);
    }

    public Timetable getTimetable(String timetableId) {
        return timetables.get(timetableId);
    }

    public void storeRoom(Room room) {
        rooms.put(room.getId(), room);
    }

    public Room getRoom(String roomId) {
        return rooms.get(roomId);
    }

    @Override
    public String toString() {
        int nrConfigs = 0, nrSubparts = 0, nrClasses = 0;

        for (Course c : courses.values()){
            for(Config conf : c.getConfigs().values()){
                nrConfigs++;
                for(Subpart s : conf.getSubparts().values()){
                    nrSubparts++;
                    for(Class ignored : s.getClasses().values()){
                        nrClasses++;
                    }
                }
            }
        }

        return String.format("nrDays = %d, slotsPerDay = %d, nrWeeks = %d", nrDays, slotsPerDay, nrWeeks) + "\n" +
                String.format("timeWeight = %d, roomWeight = %d, distributionWeight = %d", timeWeight, roomWeight, distributionWeight) + "\n" +
                String.format("nrCourses = %d, nrConfigs = %d, nrSubparts = %d, nrClasses = %d, nrTeachers = %d, nrTimetables = %d, nrRooms = %d, nrDist = %d",
                        courses.size(), nrConfigs, nrSubparts, nrClasses, teachers.size(), timetables.size(), rooms.size(), distributions.size());
    }
}
