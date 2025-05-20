package thesis.model.dbms;

import thesis.model.aggregates.ScheduledClass;
import thesis.model.aggregates.StructuredTimetableData;
import thesis.model.entities.*;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

public class DBTimetableRepository extends DBPostgreSQLManager{

    public DBTimetableRepository(String databaseName) {
        super(databaseName);
    }

    /**
     * Fetches all the data present in the database
     * @return An aggregation of the data present in the database
     */
    public StructuredTimetableData fetchTimetableData() throws SQLException {
        StructuredTimetableData data = new StructuredTimetableData();

        getTimetableConfiguration(data);

        getTeachers(data);

        getRooms(data);

        getCourses(data);

        getDistributions(data);

        getTimetables(data);

        return data;
    }

    //TODO: Completar comentário
    /**
     *
     * @param data
     * @throws SQLException
     */
    private void getTimetableConfiguration(StructuredTimetableData data) throws SQLException {
        // Obtain the optimization parameters. Assuming the data is stored correctly there should only be 1 instance of values stored
        Map<String, List<Object>> queryResult = this.read("optimization_parameters");

        int timeWeight = (int) queryResult.get("time_weight").get(0);
        int roomWeight = (int) queryResult.get("room_weight").get(0);
        int distributionWeight = (int) queryResult.get("distribution_weight").get(0);

        data.storeOptimization(timeWeight, roomWeight, distributionWeight);

        // Obtain the timetable configuration. Assuming the data is stored correctly there should only be 1 instance of values stored
        queryResult = this.read("timetable_configuration");

        int numberDays = (int) queryResult.get("number_days").get(0);
        int numberWeeks = (int) queryResult.get("number_weeks").get(0);
        int slotsPerDay = (int) queryResult.get("slots_per_day").get(0);

        data.storeConfiguration(numberDays, slotsPerDay, numberWeeks);
    }

    //TODO: Completar comentário
    /**
     *
     * @param data
     * @throws SQLException
     */
    private void getDistributions(StructuredTimetableData data) throws SQLException {
        // Obtain the distributions

    }

    //TODO: Completar comentário
    /**
     *
     * @param data
     * @throws SQLException
     */
    private void getTeachers(StructuredTimetableData data) throws SQLException {
        // Obtain and store the teachers
        Map<String, List<Object>> queryResult = this.read("teacher");
        List<Object> teacherIds = queryResult.get("id");
        List<Object> teacherNames = queryResult.get("name");

        for(int i=0; i<teacherIds.size(); i++) {
            data.storeTeacher(new Teacher((Integer) teacherIds.get(i), (String) teacherNames.get(i)));
        }

        // Obtain the unavailabilities
        queryResult = this.read("teacher_unavailability");
        List<Object> teacherUnavailIds = queryResult.get("teacher_id");
        List<Object> durations = queryResult.get("duration");
        List<Object> days = queryResult.get("days");
        List<Object> weeks = queryResult.get("weeks");
        List<Object> startSlots = queryResult.get("start_slot");

        for(int i=0; i<teacherUnavailIds.size(); i++) {
            Teacher t = data.getTeacher((Integer) teacherUnavailIds.get(i));
            t.addUnavailability(new Time((String) days.get(i),
                                          (int) startSlots.get(i),
                                          (int) durations.get(i),
                                          (String) weeks.get(i)));
        }

        // Obtain the classes the teacher will teach
        queryResult = this.read("teacher_class");
        List<Object> teacherClassIds = queryResult.get("teacher_id");
        List<Object> classIds = queryResult.get("class_id");
        List<Object> subjectIds = queryResult.get("subject_id");

        for(int i=0; i<teacherClassIds.size(); i++) {
            Teacher t = data.getTeacher((Integer) teacherClassIds.get(i));
            t.addClass((String) classIds.get(i), (String) subjectIds.get(i));
        }
    }

    //TODO: Completar comentário
    /**
     *
     * @param data
     * @throws SQLException
     */
    private void getCourses(StructuredTimetableData data) throws SQLException {
        // Obtain and store the courses
        Map<String, List<Object>> queryResult = this.read("course");
        List<Object> courseIds = queryResult.get("id");

        for (Object courseId : courseIds) {
            data.storeCourse(new Course((String) courseId));
        }

        // Obtain and store the Configs
        queryResult = this.read("config");
        List<Object> configIds = queryResult.get("id");
        List<Object> configCourseIds = queryResult.get("course_id");

        for(int i=0; i<configIds.size(); i++) {
            Config c = new Config((String) configIds.get(i));
            data.storeConfig((String) configCourseIds.get(i), c);
        }

        // Obtain and store the Subparts
        queryResult = this.read("subpart");
        List<Object> subpartIds = queryResult.get("id");
        List<Object> subpartConfigIds = queryResult.get("config_id");

        for(int i=0; i<subpartIds.size(); i++) {
            Subpart s = new Subpart((String) subpartIds.get(i));
            data.storeSubpart((String) subpartConfigIds.get(i), s);
        }
    }

    //TODO: Completar comentário
    /**
     *
     * @param data
     * @throws SQLException
     */
    private void getTimetables(StructuredTimetableData data) throws SQLException {
        // Obtain and store the Timetables
        Map<String, List<Object>> queryResult = this.read("timetable");

        List<Object> timetableId = queryResult.get("id");
        List<Object> timetableCreationDate = queryResult.get("creation_date");
        List<Object> timetableCourseId = queryResult.get("course_id");

        for(int i=0; i<timetableId.size(); i++) {
            data.storeTimetable(new Timetable((String) timetableId.get(i),
                                              (Timestamp) timetableCreationDate.get(i),
                                              (String) timetableCourseId.get(i)));
        }

        queryResult = this.read("scheduled_lesson");

        List<Object> classId = queryResult.get("id");
        List<Object> classDays = queryResult.get("days");
        List<Object> classWeeks = queryResult.get("weeks");
        List<Object> classStart = queryResult.get("start_slot");
        List<Object> classDuration = queryResult.get("duration");
        List<Object> classRoom = queryResult.get("room_id");

        for(int i=0; i<timetableId.size(); i++) {
            data.getTimetable((String) timetableId.get(i)).storeAssignedClass((String) classId.get(i),
                                                                              (String) classRoom.get(i),
                                                                              (String) classDays.get(i),
                                                                              (int) classStart.get(i),
                                                                              (int) classDuration.get(i),
                                                                              (String) classWeeks.get(i));
        }
    }

    //TODO: Completar comentário
    /**
     *
     * @param data
     * @throws SQLException
     */
    private void getRooms(StructuredTimetableData data) throws SQLException {
        // Obtain and store the rooms
        Map<String, List<Object>> queryResult = this.read("room");
        List<Object> roomIds = queryResult.get("id");

        for(Object roomId : roomIds) {
            data.storeRoom(new Room((String) roomId));
        }

        // Obtain the room travel penalizations
        queryResult = this.read("room_distance");
        List<Object> rooms_1 = queryResult.get("room_id_1");
        List<Object> rooms_2 = queryResult.get("room_id_2");
        List<Object> penalizations = queryResult.get("distance");

        for(int i=0; i<rooms_1.size(); i++){
            Room room1 = data.getRoom((String) rooms_1.get(i));
            room1.addTravel((String) rooms_2.get(i), (Integer) penalizations.get(i));
        }

        // Obtain the room unavailabilities
        queryResult = this.read("room_unavailability");
        roomIds = queryResult.get("room_id");
        List<Object> roomsUnavailWeeks = queryResult.get("weeks");
        List<Object> roomUnavailDays = queryResult.get("days");
        List<Object> roomUnavailStart = queryResult.get("start_slot");
        List<Object> roomUnavailDuration = queryResult.get("duration");

        for(int i=0; i<roomIds.size(); i++){
            Room room = data.getRoom((String) roomIds.get(i));
            room.addUnavailability(new Time((String) roomUnavailDays.get(i),
                                            (int) roomUnavailStart.get(i),
                                            (int) roomUnavailDuration.get(i),
                                            (String) roomsUnavailWeeks.get(i)));
        }
    }

    /**
     * Stores the data provided in the database. This operation is done in a database transaction
     * @param timetableData An aggregation of the data present in the application
     */
    public void storeTimetableData(StructuredTimetableData timetableData, boolean overwriteValues) {
        try {
            connection.setAutoCommit(false);

            storeTimetableConfiguration(timetableData, overwriteValues);

            storeTeachers(timetableData, overwriteValues);

            storeRooms(timetableData, overwriteValues);

            storeCourses(timetableData, overwriteValues);

            storeRestrictions(timetableData, overwriteValues);

            storeTimetables(timetableData, overwriteValues);

            connection.commit();

            connection.setAutoCommit(true);
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                System.out.println(ex);
                return;
            }
            System.out.println("An error ocurred when storing the timetable data. The transaction was rolled back.");
            System.out.println(e);
        }
    }

    //TODO: Completar comentário
    /**
     *
     * @param timetableData
     * @param overwriteValues
     * @throws SQLException
     */
    private void storeTimetableConfiguration(StructuredTimetableData timetableData, boolean overwriteValues) throws SQLException {
        // Store configurations and optimization parameters
        Map<String, Collection<?>> timetable_configuration = Map.of(
                "number_days", new ArrayList<Integer>(),
                "number_weeks", new ArrayList<Integer>(),
                "slots_per_day", new ArrayList<Integer>()
        );

        Map<String, Collection<?>> optimization_parameters = Map.of(
                "time_weight", new ArrayList<Integer>(),
                "room_weight", new ArrayList<Integer>(),
                "distribution_weight", new ArrayList<Integer>()
        );

        List<Integer> numberDays = (List<Integer>) timetable_configuration.get("number_days");
        List<Integer> numberWeeks = (List<Integer>) timetable_configuration.get("number_weeks");
        List<Integer> slotsPerDay = (List<Integer>) timetable_configuration.get("slots_per_day");

        List<Integer> timeWeight = (List<Integer>) optimization_parameters.get("time_weight");
        List<Integer> roomWeight = (List<Integer>) optimization_parameters.get("room_weight");
        List<Integer> distributionWeight = (List<Integer>) optimization_parameters.get("distribution_weight");

        int[] conf = timetableData.getConfiguration();

        numberDays.add(conf[0]);
        slotsPerDay.add(conf[1]);
        numberWeeks.add(conf[2]);

        int[] optimiz = timetableData.getOptimization();

        timeWeight.add(optimiz[0]);
        roomWeight.add(optimiz[1]);
        distributionWeight.add(optimiz[2]);

        this.insert("timetable_configuration", timetable_configuration, overwriteValues);
        this.insert("optimization_parameters", optimization_parameters, overwriteValues);
    }

    //TODO: Completar comentário
    /**
     *
     * @param timetableData
     * @param overwriteValues
     * @throws SQLException
     */
    private void storeRestrictions(StructuredTimetableData timetableData, boolean overwriteValues) throws SQLException {
        // Store restrictions
        Map<String, Collection<?>> restrictions = Map.of(
                "name", new ArrayList<String>()
        );

        Map<String, Collection<?>> restrictionSubjects = Map.of(
                "subject_id", new ArrayList<String>(),
                "restriction_id", new ArrayList<String>(),
                "penalty", new ArrayList<Integer>(),
                "hard", new ArrayList<Boolean>()
        );

        List<String> restrictionNames = (List<String>) restrictions.get("name");

        List<String> restrictionSubjectIds = (List<String>) restrictionSubjects.get("subject_id");
        List<String> restrictionIds = (List<String>) restrictionSubjects.get("restriction_id");
        List<Integer> restrictionPenalties = (List<Integer>) restrictionSubjects.get("penalty");
        List<Boolean> restrictionRequired = (List<Boolean>) restrictionSubjects.get("hard");

        for(Distribution d : timetableData.getDistributions()) {
            restrictionNames.add(d.getType());
            for(String classId : d.getInvolvedClasses()) {
                restrictionSubjectIds.add(classId);
                restrictionIds.add(d.getType());
                restrictionPenalties.add(d.getPenalty());
                restrictionRequired.add(d.getRequired());
            }
        }

        this.insert("restriction", restrictions, overwriteValues);
        this.insert("restriction_subject", restrictionSubjects, overwriteValues);
    }

    //TODO: Completar comentário
    /**
     *
     * @param timetableData
     * @param overwriteValues
     * @throws SQLException
     */
    private void storeTeachers(StructuredTimetableData timetableData, boolean overwriteValues) throws SQLException {
        // Store teachers and their unavailabilities
        Map<String, Collection<?>> teachers = Map.of(
                "id", new ArrayList<Integer>(),
                "name", new ArrayList<String>()
        );

        Map<String, Collection<?>> teacherUnavailabilities = Map.of(
                "id", new ArrayList<Integer>(),
                "days", new ArrayList<String>(),
                "weeks", new ArrayList<String>(),
                "start_slot", new ArrayList<Integer>(),
                "duration", new ArrayList<Integer>()
        );

        List<Integer> teacherIds = (List<Integer>) teachers.get("id");
        List<String> teacherNames = (List<String>) teachers.get("name");

        List<String> unavailDays = (List<String>) teacherUnavailabilities.get("days");
        List<String> unavailWeeks = (List<String>) teacherUnavailabilities.get("weeks");
        List<Integer> unavailDurations = (List<Integer>) teacherUnavailabilities.get("duration");
        List<Integer> unavailStarts = (List<Integer>) teacherUnavailabilities.get("start_slot");
        List<Integer> unavailIds = (List<Integer>) teacherUnavailabilities.get("id");

        for (Teacher teacher : timetableData.getTeachers()) {
            Integer id = teacher.getId();
            teacherIds.add(id);
            teacherNames.add(teacher.getTeacherName());

            for (Time time : teacher.getUnavails()) {
                unavailIds.add(id);
                unavailDays.add(time.getDays());
                unavailWeeks.add(time.getWeeks());
                unavailDurations.add(time.getDuration());
                unavailStarts.add(time.getStart());
            }
        }

        this.insert("teacher", teachers, overwriteValues);
        this.insert("teacher_unavailability", teacherUnavailabilities, overwriteValues);
    }

    //TODO: Completar comentário
    /**
     *
     * @param timetableData
     * @param overwriteValues
     * @throws SQLException
     */
    private void storeRooms(StructuredTimetableData timetableData, boolean overwriteValues) throws SQLException {
        // Store Rooms, their unavailabilities, the distances between them and the subjects per room
        Map<String, Collection<?>> rooms = Map.of(
                "id", new ArrayList<String>()
        );

        Map<String, Collection<?>> roomUnavails = Map.of(
                "room_id", new ArrayList<String>(),
                "days", new ArrayList<String>(),
                "weeks", new ArrayList<String>(),
                "start_slot", new ArrayList<Integer>(),
                "duration", new ArrayList<Integer>()
        );

        Map<String, Collection<?>> roomDistances = Map.of(
                "room_id_1", new ArrayList<String>(),
                "room_id_2", new ArrayList<String>(),
                "distance", new ArrayList<Integer>()
        );

//        Map<String, List<?>> subjectRooms = Map.of(
//                "subject_id", new ArrayList<String>(),
//                "room_id", new ArrayList<String>(),
//                "penalty", new ArrayList<Integer>()
//        );
        // TODO: implementar subjectRooms

        List<String> roomIds = (List<String>) rooms.get("id");

        List<String> roomUnavailIds = (List<String>) roomUnavails.get("room_id");
        List<String> roomUnavailDays = (List<String>) roomUnavails.get("days");
        List<String> roomUnavailWeeks = (List<String>) roomUnavails.get("weeks");
        List<Integer> roomUnavailStarts = (List<Integer>) roomUnavails.get("start_slot");
        List<Integer> roomUnavailDurations = (List<Integer>) roomUnavails.get("duration");

        List<String> room1Id = (List<String>) roomDistances.get("room_id_1");
        List<String> room2Id = (List<String>) roomDistances.get("room_id_2");
        List<Integer> roomDistance = (List<Integer>) roomDistances.get("distance");

//        List<String> subjectIds = (List<String>) subjectRooms.get("subject_id");
//        List<String> subjectRoomIds = (List<String>) subjectRooms.get("room_id");
//        List<Integer> subjectRoomPenalty = (List<Integer>) subjectRooms.get("penalty");

        for(Room r : timetableData.getRooms()) {
            String roomId = r.getId();
            roomIds.add(roomId);
            for(Time unavail : r.getUnavailabilities()) {
                roomUnavailIds.add(roomId);
                roomUnavailDays.add(unavail.getDays());
                roomUnavailWeeks.add(unavail.getWeeks());
                roomUnavailStarts.add(unavail.getStart());
                roomUnavailDurations.add(unavail.getDuration());
            }
            for(Map.Entry<String, Integer> entry : r.getTravelPenalizations().entrySet()) {
                room1Id.add(roomId);
                room2Id.add(entry.getKey());
                roomDistance.add(entry.getValue());
            }
        }

        this.insert("room", rooms, overwriteValues);
        this.insert("room_unavailability", roomUnavails, overwriteValues);
        this.insert("room_distance", roomDistances, overwriteValues);
    }

    //TODO: Completar comentário
    /**
     *
     * @param timetableData
     * @param overwriteValues
     * @throws SQLException
     */
    private void storeCourses(StructuredTimetableData timetableData, boolean overwriteValues) throws SQLException {
        // Store courses, configs and subparts
        Map<String, Collection<?>> courses = Map.of(
                "id", new ArrayList<String>()
        );

        Map<String, Collection<?>> configs = Map.of(
                "id", new ArrayList<String>(),
                "course_id", new ArrayList<String>()
        );

        Map<String, Collection<?>> subparts = Map.of(
                "id", new ArrayList<String>(),
                "config_id", new ArrayList<String>()
        );

        Map<String, Collection<?>> subject = Map.of(
                "id", new ArrayList<String>()
        );

        Map<String, Collection<?>> subjectSubpart = Map.of(
                "subject_id", new ArrayList<String>(),
                "subpart_id", new ArrayList<String>()
        );

        Map<String, Collection<?>> classSubject = Map.of(
                "class_id", new HashSet<String>(),
                "subject_id", new ArrayList<String>(),
                "parent_class", new ArrayList<String>()
        );

        List<String> courseIds = (List<String>) courses.get("id");

        List<String> configIds = (List<String>) configs.get("id");
        List<String> configCourseIds = (List<String>) configs.get("course_id");

        List<String> subpartsIds = (List<String>) subparts.get("id");
        List<String> confIds = (List<String>) subparts.get("config_id");

        List<String> subjects = (List<String>) subject.get("id");

        List<String> subjectSubpartIds = (List<String>) subjectSubpart.get("subpart_id");
        List<String> subpartSubjectIds = (List<String>) subjectSubpart.get("subject_id");

        Set<String> subjectClassIds = (Set<String>) classSubject.get("class_id");
        List<String> classSubjectIds = (List<String>) classSubject.get("subject_id");
        List<String> classSubjectParentClass = (List<String>) classSubject.get("parent_class");

        // store the corresponding data in each list
        for(Course course : timetableData.getCourses()) {
            courseIds.add(course.getId());
            for(Config conf : course.getConfigs()) {
                configIds.add(conf.getId());
                configCourseIds.add(course.getId());
                for(Subpart subpart : conf.getSubparts()) {
                    subpartsIds.add(subpart.getId());
                    confIds.add(conf.getId());
                    for(ClassUnit clUnit : subpart.getClasses()) {
                        String clUnitId = clUnit.getId();
                        subjects.add(clUnitId);
                        subjectClassIds.add(clUnitId);
                        classSubjectIds.add(clUnit.getSubjectId());
                        classSubjectParentClass.add(clUnit.getParentClassId());
                        subjectSubpartIds.add(subpart.getId());
                        subpartSubjectIds.add(clUnit.getSubjectId());
                    }
                }
            }
        }

        this.insert("course", courses, overwriteValues);
        this.insert("config", configs, overwriteValues);
        this.insert("subpart", subparts, overwriteValues);
        this.insert("subject", subject, overwriteValues);
        this.insert("class_subpart", classSubject, overwriteValues);
    }

    //TODO: Completar comentário
    /**
     *
     * @param timetableData
     * @param overwriteValues
     * @throws SQLException
     */
    private void storeTimetables(StructuredTimetableData timetableData, boolean overwriteValues) throws SQLException {
        // Store timetables
        Map<String, Collection<?>> timetables = Map.of(
                "id", new ArrayList<String>(),
                "creation_date", new ArrayList<String>(),
                "course_id", new ArrayList<String>()
        );

        Map<String, Collection<?>> scheduledLessons = Map.of(
                "id", new ArrayList<String>(),
                "subject_id", new ArrayList<String>(),
                "room_id", new ArrayList<String>(),
                "timetable_id", new ArrayList<String>(),
                "days", new ArrayList<String>(),
                "weeks", new ArrayList<String>(),
                "start_slot", new ArrayList<Integer>(),
                "duration", new ArrayList<Integer>()
        );

        Map<String, Collection<?>> scheduledLessonTeacher = Map.of(
                "scheduled_lesson_id", new ArrayList<String>(),
                "teacher_id", new ArrayList<Integer>()
        );

        List<String> timetableIds = (List<String>) timetables.get("id");
        List<Timestamp> timetableCreationDates = (List<Timestamp>) timetables.get("creation_date");
        List<String> timetableCourseIds = (List<String>) timetables.get("course_id");

        List<String> scheduledLessonsIds = (List<String>) scheduledLessons.get("id");
        List<String> scheduledLessonsSubjectIds = (List<String>) scheduledLessons.get("subject_id");
        List<String> scheduledLessonsRoomIds = (List<String>) scheduledLessons.get("room_id");
        List<String> scheduledLessonsTimetableIds = (List<String>) scheduledLessons.get("timetable_id");
        List<String> scheduledLessonsDays = (List<String>) scheduledLessons.get("days");
        List<String> scheduledLessonsWeeks = (List<String>) scheduledLessons.get("weeks");
        List<Integer> scheduledLessonsStart = (List<Integer>) scheduledLessons.get("start_slot");
        List<Integer> scheduledLessonsDuration = (List<Integer>) scheduledLessons.get("duration");

        List<String> scheduledLessonTeacherLessonIds = (List<String>) scheduledLessonTeacher.get("scheduled_lesson_id");
        List<Integer> scheduledLessonTeacherIds = (List<Integer>) scheduledLessonTeacher.get("teacher_id");

        for(Timetable timetable : timetableData.getTimetables()) {
            timetableIds.add(timetable.getId());
            timetableCreationDates.add(timetable.getCreationDate());
            timetableCourseIds.add(timetable.getCourseId());
            for(Map.Entry<String, ScheduledClass.ScheduledClass> entry : timetable.getAssignedClasses().entrySet()) {
                ScheduledClass.ScheduledClass scheduledClass = entry.getValue();
                Time time = scheduledClass.getScheduledTime();

                // Store the assigned class information
                scheduledLessonsIds.add(scheduledClass.getScheduledClassId());
                scheduledLessonsSubjectIds.add(entry.getKey());
                scheduledLessonsRoomIds.add(scheduledClass.getRoomId());
                scheduledLessonsTimetableIds.add(timetable.getId());
                scheduledLessonsDays.add(time.getDays());
                scheduledLessonsWeeks.add(time.getWeeks());
                scheduledLessonsStart.add(time.getStart());
                scheduledLessonsDuration.add(time.getDuration());

                // Store the assigned teachers to the class
                scheduledLessonTeacherIds.addAll(scheduledClass.getAssignedTeachers());
                if(!scheduledLessonTeacherIds.isEmpty()) {
                    scheduledLessonTeacherLessonIds.add(scheduledClass.getScheduledClassId());
                }
            }
        }

        this.insert("timetable", timetables, overwriteValues);
        this.insert("scheduled_lesson", scheduledLessons, overwriteValues);
        this.insert("scheduled_lesson_teacher", scheduledLessonTeacher, overwriteValues);
    }
}
