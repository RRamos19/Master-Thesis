package thesis.structures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StructuredTimetableData {
    // Number of days, slots per day and weeks of the timetable
    private int nrDays = 0, slotsPerDay = 0, nrWeeks = 0;

    // Optimization data
    private int timeWeight = 0, roomWeight = 0, distributionWeight = 0;

    // Storage of the data present in the ITC Format and the Database
    private final Map<String, Course> courses = new HashMap<>();              // CourseId: Course
    private final Map<Integer, Teacher> teachers = new HashMap<>();           // TeacherId: Teacher
    private final Map<String, Timetable> timetables = new HashMap<>();        // TimetableId: Timetable
    private final Map<String, Room> rooms = new HashMap<>();                  // RoomId: Room
    private final List<Distribution> distributions = new ArrayList<>();

    public void storeConfiguration(int nrDays, int slotsPerDay, int nrWeeks) {
        this.nrDays = nrDays;
        this.slotsPerDay = slotsPerDay;
        this.nrWeeks = nrWeeks;
    }

    public int[] getConfiguration() {
        int[] config = new int[3];

        config[0] = nrDays;
        config[1] = slotsPerDay;
        config[2] = nrWeeks;

        return config;
    }

    public void storeOptimization(int timeWeight, int roomWeight, int distributionWeight) {
        this.timeWeight = timeWeight;
        this.roomWeight = roomWeight;
        this.distributionWeight = distributionWeight;
    }

    public int[] getOptimization() {
        int[] optimization = new int[3];

        optimization[0] = timeWeight;
        optimization[1] = roomWeight;
        optimization[2] = distributionWeight;

        return optimization;
    }

    public void storeCourse(Course course) {
        courses.put(course.getId(), course);
    }

    public Course getCourse(String courseId) {
        return courses.get(courseId);
    }

    public Map<String, Course> getCourses() {
        return courses;
    }

    public void storeTeacher(Teacher teacher) {
        teachers.put(teacher.getId(), teacher);
    }

    public Teacher getTeacher(int teacherId) {
        return teachers.get(teacherId);
    }

    public Map<Integer, Teacher> getTeachers() {
        return teachers;
    }

    public void storeDistribution(Distribution distribution) {
        distributions.add(distribution);
    }

    public List<Distribution> getDistributions() {
        return distributions;
    }

    public void storeTimetable(Timetable timetable) {
        timetables.put(timetable.getId(), timetable);
    }

    public Timetable getTimetable(String timetableId) {
        return timetables.get(timetableId);
    }

    public Map<String, Timetable> getTimetables() {
        return timetables;
    }

    public void storeRoom(Room room) {
        rooms.put(room.getId(), room);
    }

    public Room getRoom(String roomId) {
        return rooms.get(roomId);
    }

    public Map<String, Room> getRooms() {
        return rooms;
    }

    /**
     * Merges this instance with another instance. Values that are already present on this object are overwritten with the one passed in the parameters. The values of optimization or configuration are not merged.
     * @param timetableData Instance of StructuredTimetableData to be merged with.
     */
    public void mergeWithTimetable(StructuredTimetableData timetableData) {
        if(timetableData == null) {
            // Should never happen
            throw new RuntimeException("The timetable provided is null");
        }

        for(Course c : timetableData.getCourses().values()) {
            storeCourse(c);
        }

        for(Teacher t : timetableData.getTeachers().values()) {
            storeTeacher(t);
        }

        for(Distribution d : timetableData.getDistributions()) {
            storeDistribution(d);
        }

        for(Timetable t : timetableData.getTimetables().values()) {
            storeTimetable(t);
        }

        for(Room r : timetableData.getRooms().values()) {
            storeRoom(r);
        }
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
