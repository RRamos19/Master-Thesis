package thesis.model.domain;

import thesis.model.domain.restrictions.Restriction;

import java.util.*;

public class DomainModel {
    private String problemName;
    private final TimetableConfiguration timetableConfiguration = new TimetableConfiguration();
    private final Map<String, Course> courseMap = new HashMap<>();
    private final Map<String, Config> configMap = new HashMap<>();          // These three maps are used
    private final Map<String, Subpart> subpartMap = new HashMap<>();        // to simplify the search of
    private final Map<String, ClassUnit> classUnitMap = new HashMap<>();    // specific ids
    private final List<Restriction> restrictionMap = new ArrayList<>();
    private final Map<String, Room> roomMap = new HashMap<>();
    private final Map<Integer, Teacher> teacherMap = new HashMap<>();
    private final List<Timetable> timetableList = new ArrayList<>();

    public DomainModel() {
        this.problemName = null;
    }

    public DomainModel(String problemName) {
        this.problemName = problemName;
    }

    public String getProblemName() {
        return problemName;
    }

    public void setProblemName(String problemName) {
        this.problemName = problemName;
    }

    public void setOptimizationParameters(int timeWeight, int roomWeight, int distribWeight) {
        timetableConfiguration.setTimeWeight(timeWeight);
        timetableConfiguration.setRoomWeight(roomWeight);
        timetableConfiguration.setDistribWeight(distribWeight);
    }

    public void setConfiguration(int numDays, int numWeeks, int slotPerDay) {
        timetableConfiguration.setNumDays(numDays);
        timetableConfiguration.setNumWeeks(numWeeks);
        timetableConfiguration.setSlotsPerDay(slotPerDay);
    }

    public void addRestriction(Restriction restriction) {
        restrictionMap.add(restriction);
    }

    public TimetableConfiguration getTimetableConfiguration() {
        return timetableConfiguration;
    }

    public void addRoom(Room room) {
        roomMap.put(room.getRoomId(), room);
    }

    public List<Room> getRooms() {
        return new ArrayList<>(roomMap.values());
    }

    public void addTeacher(Teacher teacher) {
        teacherMap.put(teacher.getId(), teacher);
    }

    public Teacher getTeacher(int teacherId) {
        return teacherMap.get(teacherId);
    }

    public List<Teacher> getTeachers() {
        return new ArrayList<>(teacherMap.values());
    }

    public void addCourse(Course course) {
        courseMap.put(course.getCourseId(), course);
    }

    public Course getCourse(String courseId) {
        return courseMap.get(courseId);
    }

    public List<Course> getCourses() {
        return new ArrayList<>(courseMap.values());
    }

    public void addConfig(Config config) {
        configMap.put(config.getConfigId(), config);
    }

    public void addSubpart(Subpart subpart) {
        subpartMap.put(subpart.getSubpartId(), subpart);
    }

    public void addClassUnit(ClassUnit classUnit) {
        classUnitMap.put(classUnit.getClassId(), classUnit);
    }

    public ClassUnit getClassUnit(String classUnitId) {
        return classUnitMap.get(classUnitId);
    }

    public List<ClassUnit> getClassUnits() {
        return new ArrayList<>(classUnitMap.values());
    }

    public void addTimetable(Timetable timetable) {
        timetableList.add(timetable);
    }

    public List<Restriction> getRestrictions() {
        return restrictionMap;
    }

    /**
     * Verifies if all of the data present in the model makes sense, for example, the room ids of the classes are present in the Map.
     * @return
     */
    public void verifyValidity() throws RuntimeException {
        for(ClassUnit cls : classUnitMap.values()) {
            for(int teacherId : cls.getClassTeacherList()) {
                if(teacherMap.get(teacherId) == null) {
                    throw new RuntimeException("ERROR: The teacherId " + teacherId + " in class " + cls.getClassId() + " is invalid!");
                }
            }
            for(String roomId : cls.getRoomIds()) {
                if(roomMap.get(roomId) == null) {
                    throw new RuntimeException("ERROR: The roomId " + roomId + " in class " + cls.getClassId() + " is invalid!");
                }
            }
        }
    }

    @Override
    public String toString() {
        int nrConfigs = 0, nrSubparts = 0, nrClasses = 0;

        for (Course c : courseMap.values()){
            for(Config conf : c.getConfigList()){
                nrConfigs++;
                for(Subpart s : conf.getSubpartList()){
                    nrSubparts++;
                    for(ClassUnit ignored : s.getClassUnitList()){
                        nrClasses++;
                    }
                }
            }
        }


        return String.format("nrDays = %d, slotsPerDay = %d, nrWeeks = %d", timetableConfiguration.getNumDays(), timetableConfiguration.getSlotsPerDay(), timetableConfiguration.getNumWeeks()) + "\n" +
                String.format("timeWeight = %d, roomWeight = %d, distributionWeight = %d", timetableConfiguration.getTimeWeight(), timetableConfiguration.getRoomWeight(), timetableConfiguration.getDistribWeight()) + "\n" +
                String.format("nrCourses = %d, nrConfigs = %d, nrSubparts = %d, nrClasses = %d, nrTeachers = %d, nrTimetables = %d, nrRooms = %d, nrDist = %d",
                        courseMap.size(), nrConfigs, nrSubparts, nrClasses, teacherMap.size(), timetableList.size(), roomMap.size(), restrictionMap.size());
    }
}
