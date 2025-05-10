package thesis.model.dbms;

import thesis.model.entities.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class DBTimetableRepository {
    private final int DEFAULT_TIMEOUT = 200;
    private final DBManager dbManager;
    private Connection connection = null;

    public DBTimetableRepository(DBManager dbManager) {
        this.dbManager = dbManager;
    }

    public void connect(String ip, String port, String user, String password) throws SQLException {
        connection = dbManager.connect(ip, port, user, password);
    }

    public void disconnect() throws SQLException {
        dbManager.disconnect();
        connection = null;
    }

    public boolean isConnected() throws SQLException {
        if(connection == null) {
            return false;
        }

        return connection.isValid(DEFAULT_TIMEOUT);
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

        getTimetables(data);

        return data;
    }

    /**
     * Stores the data provided in the database. This operation is done in a database transaction.
     * @param timetableData An aggregation of the data present in the application
     */
    public void storeTimetableData(StructuredTimetableData timetableData) throws SQLException {
        connection.setAutoCommit(false);

        // Insert courses, configs and subparts
        Map<String, List<?>> courses = new HashMap<>();
        Map<String, List<?>> configs = new HashMap<>();
        Map<String, List<?>> subparts = new HashMap<>();

        List<String> courseIds = new ArrayList<>();
        List<String> configIds = new ArrayList<>();
        List<String> cIds = new ArrayList<>();
        List<String> subpartsIds = new ArrayList<>();
        List<String> confIds = new ArrayList<>();
        for(Course course : timetableData.getCourses()) {
            courseIds.add(course.getId());
            for(Config conf : course.getConfigs()) {
                configIds.add(conf.getId());
                cIds.add(course.getId());
                for(Subpart subpart : conf.getSubparts()) {
                    subpartsIds.add(subpart.getId());
                    confIds.add(conf.getId());
                }
            }
        }
        courses.put("id", courseIds);
        configs.put("id", configIds);
        configs.put("course_id", cIds);
        subparts.put("id", subpartsIds);
        subparts.put("config_id", confIds);

        dbManager.insert("course", courses, true);
        dbManager.insert("config", configs, true);
        dbManager.insert("subpart", subparts, true);

        connection.setAutoCommit(true);
    }

    private void getTimetableConfiguration(StructuredTimetableData data) throws SQLException {
        // Obtain the optimization parameters. Assuming the data is stored correctly there should only be 1 instance of values stored
        Map<String, List<Object>> queryResult = dbManager.read("optimization_parameters");

        int timeWeight = (int) queryResult.get("time_weight").get(0);
        int roomWeight = (int) queryResult.get("room_weight").get(0);
        int distributionWeight = (int) queryResult.get("distribution_weight").get(0);

        data.storeOptimization(timeWeight, roomWeight, distributionWeight);

        // Obtain the timetable configuration. Assuming the data is stored correctly there should only be 1 instance of values stored
        queryResult = dbManager.read("timetable_configuration");

        int numberDays = (int) queryResult.get("number_days").get(0);
        int numberWeeks = (int) queryResult.get("number_weeks").get(0);
        int slotsPerDay = (int) queryResult.get("slots_per_day").get(0);

        data.storeConfiguration(numberDays, slotsPerDay, numberWeeks);
    }

    private void getTeachers(StructuredTimetableData data) throws SQLException {
        // Obtain and store the teachers
        Map<String, List<Object>> queryResult = dbManager.read("teacher");
        List<Object> teacherIds = queryResult.get("id");
        List<Object> teacherNames = queryResult.get("name");

        for(int i=0; i<teacherIds.size(); i++) {
            data.storeTeacher(new Teacher((Integer) teacherIds.get(i), (String) teacherNames.get(i)));
        }

        // Obtain the unavailabilities
        queryResult = dbManager.read("teacher_unavailability");
        List<Object> teacherUnavailIds = queryResult.get("teacher_id");
        List<Object> durations = queryResult.get("duration");
        List<Object> days = queryResult.get("days");
        List<Object> weeks = queryResult.get("weeks");
        List<Object> startSlots = queryResult.get("start_slot");

        for(int i=0; i<teacherUnavailIds.size(); i++) {
            Teacher t = data.getTeacher((Integer) teacherUnavailIds.get(i));
            t.addUnavailability(new Time((String) days.get(i), (int) startSlots.get(i), (int) durations.get(i), (String) weeks.get(i)));
        }

        // Obtain the classes the teacher will teach
        queryResult = dbManager.read("teacher_class");
        List<Object> teacherClassIds = queryResult.get("teacher_id");
        List<Object> classIds = queryResult.get("class_id");
        List<Object> subjectIds = queryResult.get("subject_id");

        for(int i=0; i<teacherClassIds.size(); i++) {
            Teacher t = data.getTeacher((Integer) teacherClassIds.get(i));
            t.addClass((String) classIds.get(i), (String) subjectIds.get(i));
        }
    }

    private void getCourses(StructuredTimetableData data) throws SQLException {

    }

    private void getTimetables(StructuredTimetableData data) throws SQLException {

    }

    private void getRooms(StructuredTimetableData data) throws SQLException {
        // Obtain and store the rooms
        Map<String, List<Object>> queryResult = dbManager.read("room");
        List<Object> roomIds = queryResult.get("id");

        for(Object roomId : roomIds) {
            data.storeRoom(new Room((String) roomId));
        }

        // Obtain the room travel penalizations
        queryResult = dbManager.read("room_distance");
        List<Object> rooms_1 = queryResult.get("room_id_1");
        List<Object> rooms_2 = queryResult.get("room_id_2");
        List<Object> penalizations = queryResult.get("distance");

        for(int i=0; i<rooms_1.size(); i++){
            Object room1Id = rooms_1.get(i);
            // Assuming the database is correctly configured this should never happen
            assert(room1Id != null);

            Room room1 = data.getRoom((String) room1Id);
            room1.addTravel((String) rooms_2.get(i), (Integer) penalizations.get(i));
        }

        // Obtain the room unavailabilities
        queryResult = dbManager.read("room_unavailability");
        roomIds = queryResult.get("room_id");
        List<Object> roomsUnavailWeeks = queryResult.get("weeks");
        List<Object> roomUnavailDays = queryResult.get("days");
        List<Object> roomUnavailStart = queryResult.get("start_slot");
        List<Object> roomUnavailDuration = queryResult.get("duration");

        for(int i=0; i<roomIds.size(); i++){
            Object roomId = roomIds.get(i);
            // Assuming the database is correctly configured this should never happen
            assert(roomId != null);

            Room room = data.getRoom((String) roomId);
            room.addUnavailability(new Time((String) roomUnavailDays.get(i), (Integer) roomUnavailStart.get(i), (Integer) roomUnavailDuration.get(i), (String) roomsUnavailWeeks.get(i)));
        }
    }
}
