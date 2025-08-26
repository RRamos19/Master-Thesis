package thesis.model.domain;

import thesis.model.domain.elements.*;
import thesis.model.domain.elements.exceptions.ParsingException;
import thesis.model.domain.elements.TableDisplayable;

import java.util.*;

public class DataRepository implements InMemoryRepository {
    private String programName;
    private final TimetableConfiguration timetableConfiguration = new TimetableConfiguration();
    private final Map<String, Course> courseMap = new HashMap<>();
    private final Map<String, ClassUnit> classUnitMap = new HashMap<>(); // This map is used to simplify the search of specific ids
    private final List<Constraint> constraintMap = new ArrayList<>();
    private final Map<String, Room> roomMap = new HashMap<>();
    private final Map<Integer, Teacher> teacherMap = new HashMap<>();
    private final List<Timetable> timetableList = new ArrayList<>();

    public DataRepository() {}

    public DataRepository(String programName) {
        this.programName = programName;
    }

    @Override
    public String getProgramName() {
        return programName;
    }

    @Override
    public void setProgramName(String programName) {
        this.programName = programName;
    }

    @Override
    public void setOptimizationParameters(short timeWeight, short roomWeight, short distribWeight) {
        timetableConfiguration.setTimeWeight(timeWeight);
        timetableConfiguration.setRoomWeight(roomWeight);
        timetableConfiguration.setDistribWeight(distribWeight);
    }

    @Override
    public void setConfiguration(byte numDays, short numWeeks, int slotPerDay) {
        timetableConfiguration.setNumDays(numDays);
        timetableConfiguration.setNumWeeks(numWeeks);
        timetableConfiguration.setSlotsPerDay(slotPerDay);
    }

    @Override
    public void addConstraint(Constraint constraint) {
        constraintMap.add(constraint);
    }

    @Override
    public TimetableConfiguration getTimetableConfiguration() {
        return timetableConfiguration;
    }

    @Override
    public void addRoom(Room room) {
        roomMap.put(room.getRoomId(), room);
    }

    @Override
    public List<Room> getRooms() {
        return new ArrayList<>(roomMap.values());
    }

    @Override
    public Room getRoom(String roomId) {
        return roomId != null ? roomMap.get(roomId) : null;
    }

    @Override
    public void addTeacher(Teacher teacher) {
        teacherMap.put(teacher.getId(), teacher);
    }

    @Override
    public Teacher getTeacher(int teacherId) {
        return teacherMap.get(teacherId);
    }

    @Override
    public List<Teacher> getTeachers() {
        return new ArrayList<>(teacherMap.values());
    }

    @Override
    public void addCourse(Course course) {
        courseMap.put(course.getCourseId(), course);
    }

    @Override
    public Course getCourse(String courseId) {
        return courseId != null ? courseMap.get(courseId) : null;
    }

    @Override
    public List<Course> getCourses() {
        return new ArrayList<>(courseMap.values());
    }

    @Override
    public void addClassUnit(ClassUnit classUnit) {
        classUnitMap.put(classUnit.getClassId(), classUnit);
    }

    @Override
    public ClassUnit getClassUnit(String classUnitId) {
        return classUnitId != null ? classUnitMap.get(classUnitId) : null;
    }

    @Override
    public List<ClassUnit> getClassUnits() {
        return new ArrayList<>(classUnitMap.values());
    }

    @Override
    public void addTimetable(Timetable timetable) throws ParsingException {
        timetableList.add(timetable);

        try {
            verifyValidity();
        } catch (ParsingException e) {
            timetableList.remove(timetable);
            throw e;
        }

        timetable.getScheduledLessonList().forEach((lesson) -> {
            lesson.bindModel(this);
        });
    }

    @Override
    public List<Constraint> getConstraints() {
        return constraintMap;
    }

    @Override
    public List<Timetable> getTimetableList() {
        return timetableList;
    }

    @Override
    public List<TableDisplayable> getAllDisplayableData() {
        List<Course> courses = getCourses();

        List<TableDisplayable> result = new ArrayList<>(courses);

        courses.forEach((course) -> {
            List<Config> configs = course.getConfigList();
            result.addAll(configs);

            configs.forEach((conf) -> {
                List<Subpart> subparts = conf.getSubpartList();
                result.addAll(subparts);

                subparts.forEach((sub) -> {
                    result.addAll(sub.getClassUnitList());
                });
            });
        });

        result.addAll(roomMap.values());

        result.addAll(teacherMap.values());

        result.addAll(timetableList);

        result.add(timetableConfiguration);

        return result;
    }

    /**
     * Verifies if all of the data present in the model is coherent
     * @throws ParsingException If there is an error in the model an exception is thrown explaining what went wrong
     */
    @Override
    public void verifyValidity() throws ParsingException {
        if(timetableConfiguration.getRoomWeight() == 0 &&
            timetableConfiguration.getDistribWeight() == 0 &&
            timetableConfiguration.getTimeWeight() == 0) {
            throw new ParsingException("ERROR: All the weights are at 0 which doesn't make sense.\nEither the configuration section was omitted or there is an error in the weights.");
        }

        if(programName == null) {
            throw new ParsingException("ERROR: This problem must have a name");
        }

        for(ClassUnit cls : classUnitMap.values()) {
            for(int teacherId : cls.getTeacherIdList()) {
                if(teacherMap.get(teacherId) == null) {
                    throw new ParsingException("ERROR: The teacherId " + teacherId + " in class " + cls.getClassId() + " is invalid!");
                }
            }
            for(String roomId : cls.getRoomIds()) {
                if(roomId != null && roomMap.get(roomId) == null) {
                    throw new ParsingException("ERROR: The roomId " + roomId + " in class " + cls.getClassId() + " is invalid!");
                }
            }
        }

        for(Timetable timetable : timetableList) {
            for(ScheduledLesson lesson : timetable.getScheduledLessonList()) {
                if(getClassUnit(lesson.getClassId()) == null) {
                    throw new ParsingException("ERROR: the class id " + lesson.getClassId() + " is not present in the data repository");
                }

                if(lesson.getRoomId() != null && getRoom(lesson.getRoomId()) == null) {
                    throw new ParsingException("ERROR: the room id " + lesson.getRoomId() + " is not present int the data repository");
                }

                for(int teacherId : lesson.getTeacherIds()) {
                    if(getTeacher(teacherId) == null) {
                        throw new ParsingException("ERROR: the teacher id " + teacherId + " is not present in the data repository");
                    }
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
