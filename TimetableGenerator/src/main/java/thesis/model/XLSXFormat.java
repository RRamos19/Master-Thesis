package thesis.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class XLSXFormat {
    private XLSXFormat() {}

    public enum SHEET_NAMES {
        Configuration("Configuration"),
        Classes("Classes"),
        Class_Times("Class Times"),
        Class_Rooms("Class Rooms"),
        Class_Teachers("Class Teachers"),
        Constraints("Constraints"),
        Rooms("Rooms"),
        Room_Distances("Room Distances"),
        Room_Unavailabilities("Room Unavailabilities"),
        Teachers("Teachers"),
        Teacher_Unavailabilities("Teacher Unavailabilities"),
        Solutions("Solutions"),
        Scheduled_Lessons("Scheduled Lessons");

        public final String label;

        SHEET_NAMES(String label) {
            this.label = label;
        }
    }

    public static final Map<SHEET_NAMES, List<String>> FORMAT_PER_SHEET = new LinkedHashMap<>();

    static {
        FORMAT_PER_SHEET.put(SHEET_NAMES.Configuration,            List.of("Program Name", "Number of Days", "Slots per Day", "Number of Weeks", "Time Weight", "Room Weight", "Distribution Weight"));
        FORMAT_PER_SHEET.put(SHEET_NAMES.Rooms,                    List.of("Room Id"));
        FORMAT_PER_SHEET.put(SHEET_NAMES.Room_Distances,           List.of("Room 1 Id", "Room 2 Id", "Distance Penalty"));
        FORMAT_PER_SHEET.put(SHEET_NAMES.Room_Unavailabilities,    List.of("Room Id", "Start Slot", "Length", "Days", "Weeks"));
        FORMAT_PER_SHEET.put(SHEET_NAMES.Teachers,                 List.of("Teacher Id", "Teacher Name"));
        FORMAT_PER_SHEET.put(SHEET_NAMES.Teacher_Unavailabilities, List.of("Teacher Id", "Start Slot", "Length", "Days", "Weeks"));
        FORMAT_PER_SHEET.put(SHEET_NAMES.Classes,                  List.of("Course Id", "Config Id", "Subpart Id", "Class Id", "Parent Class Id"));
        FORMAT_PER_SHEET.put(SHEET_NAMES.Class_Times,              List.of("Class Id", "Start Slot", "Length", "Days", "Weeks", "Penalty"));
        FORMAT_PER_SHEET.put(SHEET_NAMES.Class_Rooms,              List.of("Class Id", "Room Id", "Penalty"));
        FORMAT_PER_SHEET.put(SHEET_NAMES.Class_Teachers,           List.of("Class Id", "Teacher Id"));
        FORMAT_PER_SHEET.put(SHEET_NAMES.Constraints,              List.of("Constraint Id", "Constraint Name", "Penalty", "Required", "Classes"));
        FORMAT_PER_SHEET.put(SHEET_NAMES.Solutions,                List.of("Solution Id", "Runtime", "Date of Creation"));
        FORMAT_PER_SHEET.put(SHEET_NAMES.Scheduled_Lessons,        List.of("Solution Id", "Class Id", "Room Id", "Teacher Ids", "Start Slot", "Length", "Days", "Weeks"));
    }
}
