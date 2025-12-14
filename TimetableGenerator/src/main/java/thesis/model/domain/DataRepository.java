package thesis.model.domain;

import thesis.model.domain.components.*;
import thesis.model.exceptions.InvalidConfigurationException;

import java.time.LocalDateTime;
import java.util.*;

public class DataRepository implements InMemoryRepository {
    private String programName;
    private final TimetableConfiguration timetableConfiguration = new TimetableConfiguration();
    private final Map<String, Course> courseMap = new HashMap<>();
    private final Map<String, ClassUnit> classUnitMap = new HashMap<>(); // This map is used to simplify the search of specific ids
    private final Set<Constraint> constraintSet = new HashSet<>();
    private final Map<String, Room> roomMap = new HashMap<>();
    private final Map<Integer, Teacher> teacherMap = new HashMap<>();
    private final Map<UUID, Timetable> timetableMap = new HashMap<>();
    private LocalDateTime lastUpdatedAt;

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
    public void setConfiguration(short numDays, int numWeeks, short slotPerDay) {
        byte numDaysByte = (byte) numDays;
        timetableConfiguration.setNumDays(numDaysByte);
        timetableConfiguration.setNumWeeks(numWeeks);
        timetableConfiguration.setSlotsPerDay(slotPerDay);
    }

    @Override
    public void addConstraint(Constraint constraint) {
        constraintSet.add(constraint);
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
    public Collection<Room> getRooms() {
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
    public Collection<Teacher> getTeachers() {
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
    public Collection<Course> getCourses() {
        return new ArrayList<>(courseMap.values());
    }

    @Override
    public Collection<Config> getConfigs() {
        List<Config> configList = new ArrayList<>();
        for(Course course : courseMap.values()) {
            configList.addAll(course.getConfigList());
        }
        return configList;
    }

    @Override
    public Collection<Subpart> getSubparts() {
        List<Subpart> subpartList = new ArrayList<>();
        for(Config config : getConfigs()) {
            subpartList.addAll(config.getSubpartList());
        }
        return subpartList;
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
    public Collection<ClassUnit> getClassUnits() {
        return new ArrayList<>(classUnitMap.values());
    }

    @Override
    public void addTimetable(Timetable timetable) throws InvalidConfigurationException {
        timetable.bindDataModel(this);
        timetableMap.put(timetable.getTimetableId(), timetable);

        try {
            verifyValidity();
        } catch (InvalidConfigurationException e) {
            timetableMap.remove(timetable.getTimetableId());
            throw e;
        }

        for(ScheduledLesson lesson : timetable.getScheduledLessonList()) {
            ClassUnit cls = lesson.getClassUnit();

            // Corrects the time if this solution was read from a file
            // (only the duration of a time block that matches the days, weeks and start slot is copied)
            if(lesson.getLength() == 0) {
                boolean timeFixed = false;
                Time scheduledTime = lesson.getScheduledTime();
                for(Time time : cls.getTimeSet()) {
                    if(scheduledTime.getDays() == time.getDays() &&
                    scheduledTime.getWeeks() == time.getWeeks() &&
                    scheduledTime.getStartSlot() == time.getStartSlot()) {
                        lesson.fixTime(time);
                        timeFixed = true;
                        break;
                    }
                }

                if(!timeFixed) {
                    throw new InvalidConfigurationException("Time for class " + lesson + " couldn't be fixed");
                }
            }
        }
    }

    @Override
    public void removeTimetable(Timetable timetable) {
        timetableMap.remove(timetable.getTimetableId());
    }

    @Override
    public Collection<Constraint> getConstraints() {
        return new ArrayList<>(constraintSet);
    }

    @Override
    public Collection<Timetable> getTimetableList() {
        return new ArrayList<>(timetableMap.values());
    }

    @Override
    public Timetable getTimetable(UUID id) {
        return timetableMap.get(id);
    }

    @Override
    public void setLastUpdatedAt() {
        this.lastUpdatedAt = LocalDateTime.now();
    }

    @Override
    public void setLastUpdatedAt(LocalDateTime updateTime) {
        this.lastUpdatedAt = updateTime;
    }

    @Override
    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    @Override
    public void cleanUnusedData() {
        Map<Constraint, Boolean> constraintsToRemove = new HashMap<>();
        Map<String, Boolean> roomsToRemove = new HashMap<>();
        Map<Integer, Boolean> teachersToRemove = new HashMap<>();

        for(Constraint c : constraintSet) {
            constraintsToRemove.put(c, true);
        }
        for(Room r : roomMap.values()) {
            roomsToRemove.put(r.getRoomId(), true);
        }
        for(Teacher t : teacherMap.values()) {
            teachersToRemove.put(t.getId(), true);
        }

        for(ClassUnit cls : classUnitMap.values()) {
            for(Constraint c : cls.getConstraintList()) {
                constraintsToRemove.put(c, false);
            }

            for(String roomId : cls.getClassRoomPenalties().keySet()) {
                roomsToRemove.put(roomId, false);
            }

            for(Integer teacherId : cls.getTeacherIdList()) {
                teachersToRemove.put(teacherId, false);
            }
        }

        constraintsToRemove.forEach((constraint, remove) -> {
            if(remove) {
                constraintSet.remove(constraint);
            }
        });
        roomsToRemove.forEach((roomId, remove) -> {
            if(remove) {
                roomMap.remove(roomId);
            }
        });
        teachersToRemove.forEach((teacherId, remove) -> {
            if(remove) {
                teacherMap.remove(teacherId);
            }
        });

        // Remove the roomDistances for rooms that are no longer stored
        for(Room room : roomMap.values()) {
            for(String room2Id : new ArrayList<>(room.getRoomDistances().keySet())) {
                if(!roomMap.containsKey(room2Id)) {
                    room.removeRoomDistance(room2Id);
                }
            }
        }
    }

    @Override
    public void setRoomBidirectionalDistances() {
        // Add bidirectionality to room distances
        for(Room room1 : roomMap.values()) {
            String roomId = room1.getRoomId();

            room1.getRoomDistances().forEach((room2Id, distance) -> {
                Room room2 = roomMap.get(room2Id);

                if(room2 == null) {
                    throw new RuntimeException("Error while setting room distances. Room " + room2Id + " not found.");
                }

                room2.addRoomDistance(roomId, distance);
            });
        }

        // Now that the bidirectionality is done populate the new map that was made
        // to optimize the lookups of room distances
        for(Room room : roomMap.values()) {
            room.optimizeRoomDistances();
        }
    }

    /**
     * Verifies if all the data is present in the model is coherent
     * @throws InvalidConfigurationException If there is an error in the model an exception is thrown explaining what went wrong
     */
    @Override
    public void verifyValidity() throws InvalidConfigurationException {
        if(timetableConfiguration.getRoomWeight() == 0 &&
            timetableConfiguration.getDistribWeight() == 0 &&
            timetableConfiguration.getTimeWeight() == 0) {
            throw new InvalidConfigurationException("Problem configuration - All the weights are at 0 which doesn't make sense.\nEither the configuration section was omitted or there is an error in the weights.");
        }

        if(programName == null) {
            throw new InvalidConfigurationException("Problem configuration - This problem must have a name");
        }

        for(ClassUnit cls : classUnitMap.values()) {
            for(int teacherId : cls.getTeacherIdList()) {
                if(teacherMap.get(teacherId) == null) {
                    throw new InvalidConfigurationException("Problem configuration - The teacherId " + teacherId + " in class " + cls.getClassId() + " wasn't defined!");
                }
            }
            for(String roomId : cls.getRoomIds()) {
                if(roomId != null && roomMap.get(roomId) == null) {
                    throw new InvalidConfigurationException("Problem configuration - The roomId " + roomId + " in class " + cls.getClassId() + " wasn't defined!");
                }
            }
        }

        for(Room room1 : roomMap.values()) {
            for(String room2Id : room1.getRoomDistances().keySet()) {
                if(roomMap.get(room2Id) == null) {
                    throw new InvalidConfigurationException("Problem configuration - The roomId " + room2Id + " found in the distance section of room " + room1.getRoomId() + " wasn't defined!");
                }
            }
        }

        for(Timetable timetable : timetableMap.values()) {
            for(ScheduledLesson lesson : timetable.getScheduledLessonList()) {
                ClassUnit cls = getClassUnit(lesson.getClassId());

                if(cls == null) {
                    throw new InvalidConfigurationException("Timetable - The class id " + lesson.getClassId() + " is not present in the data repository");
                }

                if(lesson.getRoomId() != null && getRoom(lesson.getRoomId()) == null) {
                    throw new InvalidConfigurationException("Timetable - The room id " + lesson.getRoomId() + " is not present int the data repository");
                }

                for(int teacherId : lesson.getTeacherIds()) {
                    if (getTeacher(teacherId) == null) {
                        throw new InvalidConfigurationException("Timetable - The teacher id " + teacherId + " is not present in the data repository");
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
                        courseMap.size(), nrConfigs, nrSubparts, nrClasses, teacherMap.size(), timetableMap.size(), roomMap.size(), constraintSet.size());
    }
}
