package thesis.model.entities;

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

    // Only used to simplify the search of configs and subparts
    private final Map<String, Config> configs = new HashMap<>();              // ConfigId: Config
    private final Map<String, Subpart> subparts = new HashMap<>();            // SubpartId: Subpart

    public void storeConfiguration(int nrDays, int slotsPerDay, int nrWeeks) {
        this.nrDays = nrDays;
        this.slotsPerDay = slotsPerDay;
        this.nrWeeks = nrWeeks;
    }

    public void storeConfiguration(int[] configurationValues) {
        if(configurationValues.length != 3) {
            throw new IllegalArgumentException("Configuration array must have a length of 3 elements");
        }

        this.nrDays = configurationValues[0];
        this.slotsPerDay = configurationValues[1];
        this.nrWeeks = configurationValues[2];
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

    public void storeOptimization(int[] optimizationValues) {
        if(optimizationValues.length != 3) {
            throw new IllegalArgumentException("Optimization array must have a length of 3 elements");
        }

        this.timeWeight = optimizationValues[0];
        this.roomWeight = optimizationValues[1];
        this.distributionWeight = optimizationValues[2];
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

    public List<Course> getCourses() {
        return new ArrayList<>(courses.values());
    }

    public void storeConfig(String courseId, Config config) {
        getCourse(courseId).addConfig(config);
        configs.put(config.getId(), config);
    }

    public Config getConfig(String configId) {
        return configs.get(configId);
    }

    public List<Config> getConfigs() {
        return new ArrayList<>(configs.values());
    }

    public void storeSubpart(String configId, Subpart subpart) {
        getConfig(configId).addSubpart(subpart);
        subparts.put(subpart.getId(), subpart);
    }

    public Subpart getSubpart(String subpartId) {
        return subparts.get(subpartId);
    }

    public List<Subpart> getSubparts() {
        return new ArrayList<>(subparts.values());
    }

    public void storeTeacher(Teacher teacher) {
        teachers.put(teacher.getId(), teacher);
    }

    public Teacher getTeacher(int teacherId) {
        return teachers.get(teacherId);
    }

    public List<Teacher> getTeachers() {
        return new ArrayList<>(teachers.values());
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

    public List<Timetable> getTimetables() {
        return new ArrayList<>(timetables.values());
    }

    public void storeRoom(Room room) {
        rooms.put(room.getId(), room);
    }

    public Room getRoom(String roomId) {
        return rooms.get(roomId);
    }

    public List<Room> getRooms() {
        return new ArrayList<>(rooms.values());
    }

    /**
     * Merges this instance with another instance. Values that are already present on this object are overwritten with the one passed in the parameters. The values of optimization or configuration are not merged.
     * @param timetableData Instance of StructuredTimetableData to be merged with.
     */
    public void mergeWithTimetable(StructuredTimetableData timetableData) {
        if(timetableData == null) {
            // Should never happen
            throw new IllegalArgumentException("The timetable provided is null");
        }

        for(Course c : timetableData.getCourses()) {
            storeCourse(c);
        }

        for(Teacher t : timetableData.getTeachers()) {
            storeTeacher(t);
        }

        for(Distribution d : timetableData.getDistributions()) {
            storeDistribution(d);
        }

        for(Timetable t : timetableData.getTimetables()) {
            storeTimetable(t);
        }

        for(Room r : timetableData.getRooms()) {
            storeRoom(r);
        }
    }

    @Override
    public String toString() {
        int nrConfigs = 0, nrSubparts = 0, nrClasses = 0;

        for (Course c : courses.values()){
            for(Config conf : c.getConfigs()){
                nrConfigs++;
                for(Subpart s : conf.getSubparts()){
                    nrSubparts++;
                    for(ClassUnit ignored : s.getClasses()){
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
