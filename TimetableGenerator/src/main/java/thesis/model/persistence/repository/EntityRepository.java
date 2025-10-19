package thesis.model.persistence.repository;

import thesis.model.persistence.repository.entities.*;

import java.util.*;

public class EntityRepository {
    // Contains programName, configuration and optimization of the program
    private ProgramEntity programEntity;

    // Storage of the data present in the ITC Format and the Database
    private final Map<String, CourseEntity> courses = new HashMap<>();          // CourseId: Course
    private final Map<Integer, TeacherEntity> teachers = new HashMap<>();       // TeacherId: Teacher
    private final Map<Integer, TimetableEntity> timetables = new HashMap<>();   // TimetableId: Timetable
    private final Map<String, RoomEntity> rooms = new HashMap<>();              // RoomName: Room
    private final Set<ConstraintEntity> constraintEntities = new HashSet<>();

    // Only used to simplify the search of classes
    private final Map<String, ClassUnitEntity> classUnits = new HashMap<>();    // ClassId: ClassUnit

    public void setProgramName(String programName) {
        programEntity.setName(programName);
    }

    public String getProgramName() {
        return programEntity.getName();
    }

    public void storeProgram(ProgramEntity programEntity) {
        this.programEntity = programEntity;
    }

    public ProgramEntity getProgramEntity() {
        return programEntity;
    }

    public void storeCourse(CourseEntity courseEntity) {
        courses.put(courseEntity.getName(), courseEntity);

        for(ConfigEntity configEntity : courseEntity.getConfigSet()) {
            for(SubpartEntity subpartEntity : configEntity.getSubpartSet()) {
                for(ClassUnitEntity classUnitEntity : subpartEntity.getClassUnitSet()) {
                    storeClassUnit(classUnitEntity);
                }
            }
        }
    }

    public Collection<CourseEntity> getCourses() {
        return new ArrayList<>(courses.values());
    }

    public void storeClassUnit(ClassUnitEntity classUnitEntity) {
        classUnits.put(classUnitEntity.getName(), classUnitEntity);
    }

    public ClassUnitEntity getClassUnit(String className) {
        return classUnits.get(className);
    }

    public void storeTeacher(TeacherEntity teacherEntity) {
        teachers.put(teacherEntity.getId(), teacherEntity);
    }

    public TeacherEntity getTeacher(int teacherId) {
        return teachers.get(teacherId);
    }

    public Collection<TeacherEntity> getTeachers() {
        return teachers.values();
    }

    public void storeConstraintEntity(ConstraintEntity constraintEntity) {
        constraintEntities.add(constraintEntity);
    }

    public Collection<ConstraintEntity> getConstraintEntities() {
        return new ArrayList<>(constraintEntities);
    }

    public void storeTimetable(TimetableEntity timetableEntity) {
        timetables.put(timetableEntity.getId(), timetableEntity);
    }

    public TimetableEntity getTimetable(int timetableId) {
        return timetables.get(timetableId);
    }

    public Collection<TimetableEntity> getTimetables() {
        return new ArrayList<>(timetables.values());
    }

    public void storeRoom(RoomEntity roomEntity) {
        rooms.put(roomEntity.getName(), roomEntity);
    }

    public RoomEntity getRoom(String roomName) {
        return rooms.get(roomName);
    }

    public List<RoomEntity> getRooms() {
        return new ArrayList<>(rooms.values());
    }

    @Override
    public String toString() {
        int nrConfigs = 0, nrSubparts = 0, nrClasses = 0;

        for (CourseEntity c : courses.values()){
            for(ConfigEntity conf : c.getConfigSet()){
                nrConfigs++;
                for(SubpartEntity s : conf.getSubpartSet()){
                    nrSubparts++;
                    for(ClassUnitEntity ignored : s.getClassUnitSet()){
                        nrClasses++;
                    }
                }
            }
        }


        return "program = " + programEntity.getName() + '\n' +
                String.format("nrDays = %d, slotsPerDay = %d, nrWeeks = %d", programEntity.getNumberDays(), programEntity.getSlotsPerDay(), programEntity.getNumberWeeks()) + "\n" +
                String.format("timeWeight = %d, roomWeight = %d, distributionWeight = %d", programEntity.getTimeWeight(), programEntity.getRoomWeight(), programEntity.getDistributionWeight()) + "\n" +
                String.format("nrCourses = %d, nrConfigs = %d, nrSubparts = %d, nrClasses = %d, nrTeachers = %d, nrTimetables = %d, nrRooms = %d, nrDist = %d",
                        courses.size(), nrConfigs, nrSubparts, nrClasses, teachers.size(), timetables.size(), rooms.size(), constraintEntities.size());
    }
}
