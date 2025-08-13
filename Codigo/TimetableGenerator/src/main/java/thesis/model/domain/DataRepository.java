package thesis.model.domain;

import java.util.*;

public class DataRepository {
    private String programName;
    private final TimetableConfiguration timetableConfiguration = new TimetableConfiguration();
    private final Map<String, Course> courseMap = new HashMap<>();
    private final Map<String, ClassUnit> classUnitMap = new HashMap<>(); // This map is used to simplify the search of specific ids
    private final List<Constraint> constraintMap = new ArrayList<>();
    private final Map<String, Room> roomMap = new HashMap<>();
    private final Map<Integer, Teacher> teacherMap = new HashMap<>();
    private final List<Timetable> timetableList = new ArrayList<>();

    public DataRepository() {
        this.programName = null;
    }

    public DataRepository(String programName) {
        this.programName = programName;
    }

    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public void setOptimizationParameters(short timeWeight, short roomWeight, short distribWeight) {
        timetableConfiguration.setTimeWeight(timeWeight);
        timetableConfiguration.setRoomWeight(roomWeight);
        timetableConfiguration.setDistribWeight(distribWeight);
    }

    public void setConfiguration(byte numDays, short numWeeks, int slotPerDay) {
        timetableConfiguration.setNumDays(numDays);
        timetableConfiguration.setNumWeeks(numWeeks);
        timetableConfiguration.setSlotsPerDay(slotPerDay);
    }

    public void addConstraint(Constraint constraint) {
        constraintMap.add(constraint);
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

    public Room getRoom(String roomId) {
        return roomMap.get(roomId);
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
        timetable.setProgram(programName);
        timetableList.add(timetable);
    }

    public List<Constraint> getConstraints() {
        return constraintMap;
    }

    public List<Timetable> getTimetableList() {
        return timetableList;
    }

    /**
     * Verifies if all of the data present in the model is coherent
     */
    public void verifyValidity() throws RuntimeException {
        if(programName == null) {
            throw new RuntimeException("ERROR: This problem must have a name");
        }

        for(ClassUnit cls : classUnitMap.values()) {
            for(int teacherId : cls.getTeacherIdList()) {
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
                        courseMap.size(), nrConfigs, nrSubparts, nrClasses, teacherMap.size(), timetableList.size(), roomMap.size(), constraintMap.size());
    }
}
