package thesis.model.persistence;

import thesis.model.persistence.entities.*;

import java.util.*;

public class EntityModel {
    // Number of days, slots per day and weeks of the timetable
    private ConfigurationEntity configurationEntity;

    // Optimization data
    private OptimizationParametersEntity optimizationParametersEntity;

    // Storage of the data present in the ITC Format and the Database
    private final Map<String, CourseEntity> courses = new HashMap<>();              // CourseId: Course
    private final Map<Integer, TeacherEntity> teachers = new HashMap<>();           // TeacherId: Teacher
    private final Map<UUID, TimetableEntity> timetables = new HashMap<>();          // TimetableId: Timetable
    private final Map<String, RoomEntity> rooms = new HashMap<>();                  // RoomName: Room
    private final Map<String, ConstraintTypeEntity> constraintTypes = new HashMap<>();    // RestrictionName: Restriction

    // Only used to simplify the search of configs, subparts and classes
    private final Map<String, ConfigEntity> configs = new HashMap<>();                // ConfigId: Config
    private final Map<String, SubpartEntity> subparts = new HashMap<>();              // SubpartId: Subpart
    private final Map<String, ClassUnitEntity> classUnits = new HashMap<>();          // ClassId: ClassUnit

    public void storeConfiguration(ConfigurationEntity configurationEntity) {
        this.configurationEntity = configurationEntity;
    }

    public ConfigurationEntity getConfiguration() {
        return configurationEntity;
    }

    public void storeOptimization(OptimizationParametersEntity optimizationParametersEntity) {
        this.optimizationParametersEntity = optimizationParametersEntity;
    }

    public OptimizationParametersEntity getOptimization() {
        return optimizationParametersEntity;
    }

    public void storeCourse(CourseEntity courseEntity) {
        courses.put(courseEntity.getName(), courseEntity);
    }

    public CourseEntity getCourse(String courseId) {
        return courses.get(courseId);
    }

    public List<CourseEntity> getCourses() {
        return new ArrayList<>(courses.values());
    }

    public void storeConfig(ConfigEntity configEntity) {
        configs.put(configEntity.getName(), configEntity);
    }

    public ConfigEntity getConfig(String configId) {
        return configs.get(configId);
    }

    public List<ConfigEntity> getConfigs() {
        return new ArrayList<>(configs.values());
    }

    public void storeSubpart(SubpartEntity subpartEntity) {
        subparts.put(subpartEntity.getName(), subpartEntity);
    }

    public SubpartEntity getSubpart(String subpartId) {
        return subparts.get(subpartId);
    }

    public List<SubpartEntity> getSubparts() {
        return new ArrayList<>(subparts.values());
    }

    public void storeClassUnit(ClassUnitEntity classUnitEntity) {
        classUnits.put(classUnitEntity.getName(), classUnitEntity);
    }

    public ClassUnitEntity getClassUnit(String className) {
        return classUnits.get(className);
    }

    public List<ClassUnitEntity> getClassUnits() {
        return new ArrayList<>(classUnits.values());
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

    public void storeConstraintType(ConstraintTypeEntity constraintTypeEntity) {
        constraintTypes.put(constraintTypeEntity.getName(), constraintTypeEntity);
    }

    public ConstraintTypeEntity getConstraintType(String restrictionName) {
        return constraintTypes.get(restrictionName);
    }

    public List<ConstraintTypeEntity> getConstraintTypes() {
        return new ArrayList<>(constraintTypes.values());
    }

    public void storeTimetable(TimetableEntity timetableEntity) {
        timetables.put(timetableEntity.getId(), timetableEntity);
    }

    public TimetableEntity getTimetable(UUID timetableId) {
        return timetables.get(timetableId);
    }

    public List<TimetableEntity> getTimetables() {
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

    /**
     * Merges this instance with another instance. Values that are already present on this object are overwritten with the one passed in the parameters. The values of optimization or configuration are not merged.
     * @param timetableData Instance of StructuredTimetableData to be merged with.
     */
    public void mergeWithTimetable(EntityModel timetableData) {
        if(timetableData == null) {
            // Should never happen
            throw new IllegalArgumentException("The timetable provided is null");
        }

        for(CourseEntity c : timetableData.getCourses()) {
            storeCourse(c);
        }

        for(TeacherEntity t : timetableData.getTeachers()) {
            storeTeacher(t);
        }

        for(ConstraintTypeEntity r : timetableData.getConstraintTypes()) {
            storeConstraintType(r);
        }

        for(TimetableEntity t : timetableData.getTimetables()) {
            storeTimetable(t);
        }

        for(RoomEntity r : timetableData.getRooms()) {
            storeRoom(r);
        }
    }

    @Override
    public String toString() {
        int nrConfigs = 0, nrSubparts = 0, nrClasses = 0;

        for (CourseEntity c : courses.values()){
            for(ConfigEntity conf : c.getConfigList()){
                nrConfigs++;
                for(SubpartEntity s : conf.getSubpartList()){
                    nrSubparts++;
                    for(ClassUnitEntity ignored : s.getClassUnits()){
                        nrClasses++;
                    }
                }
            }
        }


        return String.format("nrDays = %d, slotsPerDay = %d, nrWeeks = %d", configurationEntity.getNumberDays(), configurationEntity.getSlotsPerDay(), configurationEntity.getNumberWeeks()) + "\n" +
                String.format("timeWeight = %d, roomWeight = %d, distributionWeight = %d", optimizationParametersEntity.getTimeWeight(), optimizationParametersEntity.getRoomWeight(), optimizationParametersEntity.getDistributionWeight()) + "\n" +
                String.format("nrCourses = %d, nrConfigs = %d, nrSubparts = %d, nrClasses = %d, nrTeachers = %d, nrTimetables = %d, nrRooms = %d, nrDist = %d",
                        courses.size(), nrConfigs, nrSubparts, nrClasses, teachers.size(), timetables.size(), rooms.size(), constraintTypes.size());
    }
}
