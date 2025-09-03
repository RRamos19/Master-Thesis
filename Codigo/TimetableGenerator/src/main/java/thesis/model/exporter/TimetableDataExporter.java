package thesis.model.exporter;

import thesis.model.domain.InMemoryRepository;
import thesis.model.domain.elements.*;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimetableDataExporter implements DataExporter {
    private static final String EXPORT_LOCATION_PATH = "exports/";

    private final File exportLocation;
    private final int INDENT_SIZE = 2;
    private final Charset STANDARD_CHARSET = StandardCharsets.UTF_8;

    public TimetableDataExporter() {
        exportLocation = new File(EXPORT_LOCATION_PATH);

        if(!exportLocation.exists()) {
            if(!exportLocation.mkdir()) {
                throw new IllegalStateException("There was an error while creation a folder in path " + exportLocation.getAbsolutePath());
            }
        }
    }

    private File avoidFileOverwriting(String name) {
        int solutionExportNumber = 0;
        File file;
        do {
            // Checks if the file exists. If the answer is true then a
            // number between brackets is added to avoid overwriting an existing file
            String exportNumberString = solutionExportNumber >= 1 ? " (" + solutionExportNumber + ")" : "";
            file = new File(exportLocation.getAbsolutePath() + '/' + name + exportNumberString + ".xml");
            solutionExportNumber++;
        } while(file.exists());

        return file;
    }

    private String addIndentation(int indentation, String str) {
        int finalStringSize = indentation + str.length();
        return String.format("%" + finalStringSize + "s", str);
    }

    private void writeToFile(File file, String content) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), STANDARD_CHARSET))) {
            bw.write(content);
        }
    }

    public String getExportLocation() {
        return exportLocation.getAbsolutePath();
    }

    @Override
    public void exportSolutionsToITC(InMemoryRepository data) throws IOException {
        String fileName = "solution_" + data.getProgramName();
        for(Timetable timetable : data.getTimetableList()) {
            File file = avoidFileOverwriting(fileName);
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            String formattedNow = now.format(formatter);
            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append("<solution xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"solution\" name=\"")
                    .append(data.getProgramName())
                    .append("\" runtime=\"").append(timetable.getRuntime())
                    .append("\" timeStampUTC12=\"").append(formattedNow).append("\">\n");

            Map<Teacher, List<String>> teacherClassMap = new HashMap<>();
            for (ScheduledLesson scheduledLesson : timetable.getScheduledLessonList()) {
                String classId = scheduledLesson.getClassId();
                stringBuilder.append(addIndentation(INDENT_SIZE, "<class id=\"")).append(classId).append("\" ");

                List<Teacher> teacherList = scheduledLesson.getTeachers();
                if(!teacherList.isEmpty()) {
                    for(Teacher teacher : teacherList) {
                        List<String> classList = teacherClassMap.computeIfAbsent(teacher, k -> new ArrayList<>());

                        classList.add(classId);
                    }
                }

                if(scheduledLesson.getRoomId() != null) {
                    stringBuilder.append("room=\"").append(scheduledLesson.getRoomId()).append("\" ");
                }

                stringBuilder.append("days=\"").append(scheduledLesson.getDaysBinaryString()).append("\" ")
                        .append("start=\"").append(scheduledLesson.getStartSlot()).append("\" ")
                        .append("weeks=\"").append(scheduledLesson.getWeeksBinaryString()).append("\"")
                        .append("/>\n");
            }

            if(!teacherClassMap.isEmpty()) {
                int indent = INDENT_SIZE;
                stringBuilder.append(addIndentation(indent, "<teachers>\n"));

                indent += INDENT_SIZE;
                for(Map.Entry<Teacher, List<String>> teacherClass : teacherClassMap.entrySet()) {
                    stringBuilder.append(addIndentation(indent, "<teacher id=\""))
                            .append(teacherClass.getKey().getId()).append("\" ")
                            .append("name=\"").append(teacherClass.getKey().getName())
                            .append("\">\n");

                    indent += INDENT_SIZE;
                    for(String classId : teacherClass.getValue()) {
                        stringBuilder.append(addIndentation(indent, "<class id=\""))
                                .append(classId)
                                .append("\"/>\n");
                    }
                    indent -= INDENT_SIZE;

                    stringBuilder.append(addIndentation(indent, "</teacher>\n"));
                }
                indent -= INDENT_SIZE;

                stringBuilder.append(addIndentation(indent, "</teachers>\n"));
            }

            stringBuilder.append("</solution>");

            writeToFile(file, stringBuilder.toString());
        }
    }

    @Override
    public void exportDataToITC(InMemoryRepository data) throws IOException {
        File file = avoidFileOverwriting(data.getProgramName());
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                .append("<!DOCTYPE problem PUBLIC \"-//ITC 2019//DTD Problem Format/EN\" \"http://www.itc2019.org/competition-format.dtd\">\n\n");

        TimetableConfiguration timetableConfiguration = data.getTimetableConfiguration();
        byte numDays = timetableConfiguration.getNumDays();
        short numWeeks = timetableConfiguration.getNumWeeks();
        byte indentation = 0;

        // Problem configuration
        stringBuilder.append("<problem name=\"").append(data.getProgramName()).append("\" ")
                .append("nrDays=\"").append(timetableConfiguration.getNumDays()).append("\" ")
                .append("slotsPerDay=\"").append(timetableConfiguration.getSlotsPerDay()).append("\" ")
                .append("nrWeeks=\"").append(timetableConfiguration.getNumWeeks()).append("\">\n");

        indentation += INDENT_SIZE;

        // Distributions weights
        stringBuilder.append(addIndentation(indentation, "<optimization time=\"")).append(timetableConfiguration.getTimeWeight()).append("\" ")
                .append("room=\"").append(timetableConfiguration.getRoomWeight()).append("\" ")
                .append("distribution=\"").append(timetableConfiguration.getDistribWeight()).append("\" ")
                .append("student=\"").append(0).append("\"/>\n");

        // Rooms
        List<Room> roomList = data.getRooms();
        if(!roomList.isEmpty()) {
            stringBuilder.append(addIndentation(indentation, "<rooms>\n"));
            indentation += INDENT_SIZE;
            for (Room room : roomList) {
                stringBuilder.append(addIndentation(indentation, "<room id=\"")).append(room.getRoomId());

                Map<String, Integer> roomDistances = room.getRoomDistances();
                List<Time> roomUnavailabilities = room.getRoomUnavailabilities();

                if(!(roomDistances.isEmpty() && roomUnavailabilities.isEmpty())) {
                    stringBuilder.append("\">\n");

                    indentation += INDENT_SIZE;

                    for (Map.Entry<String, Integer> roomDistance : roomDistances.entrySet()) {
                        stringBuilder.append(addIndentation(indentation, "<travel room=\"")).append(roomDistance.getKey()).append("\" ")
                                .append("value=\"").append(roomDistance.getValue()).append("\"/>\n");
                    }

                    for (Time roomTime : roomUnavailabilities) {
                        stringBuilder.append(addIndentation(indentation, "<unavailable ")).append(roomTime.toString(numDays, numWeeks)).append("/>\n");
                    }

                    indentation -= INDENT_SIZE;

                    stringBuilder.append(addIndentation(indentation, "</room>\n"));
                } else {
                    // If either of the sub sections are empty then the tag is closed in a single line
                    stringBuilder.append("\"/>\n");
                }
            }
            indentation -= INDENT_SIZE;
            stringBuilder.append(addIndentation(indentation, "</rooms>\n"));
        }

        // Course, configs, subparts and classes
        List<Course> courseList = data.getCourses();
        if(!courseList.isEmpty()) {
            stringBuilder.append(addIndentation(indentation, "<courses>\n"));
            indentation += INDENT_SIZE;
            for (Course course : courseList) {
                stringBuilder.append(addIndentation(indentation, "<course id=\"")).append(course.getCourseId()).append("\">\n");
                indentation += INDENT_SIZE;
                for (Config config : course.getConfigList()) {
                    stringBuilder.append(addIndentation(indentation, "<config id=\"")).append(config.getConfigId()).append("\">\n");
                    indentation += INDENT_SIZE;
                    for (Subpart subpart : config.getSubpartList()) {
                        stringBuilder.append(addIndentation(indentation, "<subpart id=\"")).append(subpart.getSubpartId()).append("\">\n");
                        indentation += INDENT_SIZE;
                        for (ClassUnit classUnit : subpart.getClassUnitList()) {
                            stringBuilder.append(addIndentation(indentation, "<class id=\"")).append(classUnit.getClassId()).append("\">\n");

                            indentation += INDENT_SIZE;
                            for (Map.Entry<String, Integer> classRoomPenalties : classUnit.getClassRoomPenalties().entrySet()) {
                                stringBuilder.append(addIndentation(indentation, "<room id=\"")).append(classRoomPenalties.getKey()).append("\" ")
                                        .append("penalty=\"").append(classRoomPenalties.getValue()).append("\"/>\n");
                            }

                            for (Map.Entry<Time, Integer> classTime : classUnit.getClassTimePenalties().entrySet()) {
                                stringBuilder.append(addIndentation(indentation, "<time ")).append(classTime.getKey().toString(numDays, numWeeks))
                                        .append(" penalty=\"").append(classTime.getValue()).append("\"/>\n");
                            }
                            indentation -= INDENT_SIZE;

                            stringBuilder.append(addIndentation(indentation, "</class>\n"));
                        }
                        indentation -= INDENT_SIZE;
                        stringBuilder.append(addIndentation(indentation, "</subpart>\n"));
                    }
                    indentation -= INDENT_SIZE;
                    stringBuilder.append(addIndentation(indentation, "</config>\n"));
                }
                indentation -= INDENT_SIZE;
                stringBuilder.append(addIndentation(indentation, "</course>\n"));
            }
            indentation -= INDENT_SIZE;
            stringBuilder.append(addIndentation(indentation, "</courses>\n"));
        }

        // Teachers
        List<Teacher> teacherList = data.getTeachers();
        if(!teacherList.isEmpty()) {
            stringBuilder.append(addIndentation(indentation,"<teachers>\n"));
            indentation += INDENT_SIZE;
            for (Teacher teacher : teacherList) {
                stringBuilder.append(addIndentation(indentation, "<teacher id=\""))
                        .append(teacher.getId())
                        .append("\" name=\"")
                        .append(teacher.getName())
                        .append("\">\n");

                indentation += INDENT_SIZE;
                for(String classId : teacher.getTeacherClassList()) {
                    stringBuilder.append(addIndentation(indentation, "<class id=\""))
                            .append(classId)
                            .append("\"/>\n");
                }
                indentation -= INDENT_SIZE;

                stringBuilder.append(addIndentation(indentation, "</teacher>\n"));
            }
            indentation -= INDENT_SIZE;

            stringBuilder.append(addIndentation(indentation, "</teachers>\n"));
        }

        // Constraints
        List<Constraint> constraintList = data.getConstraints();
        if(!constraintList.isEmpty()) {
            stringBuilder.append(addIndentation(indentation, "<distributions>\n"));
            indentation += INDENT_SIZE;
            for(Constraint constraint : constraintList) {
                stringBuilder.append(addIndentation(indentation, "<distribution type=\"")).append(constraint.getType());

                Integer firstParam = constraint.getFirstParameter();
                Integer secondParam = constraint.getSecondParameter();

                // If the second parameter is not null then surely the first is not null either
                if (firstParam != null) {
                    stringBuilder.append("(").append(firstParam);

                    if (secondParam != null) {
                        stringBuilder.append(",").append(secondParam);
                    }

                    stringBuilder.append(")");
                }

                stringBuilder.append("\"");

                if(constraint.getRequired()) {
                    stringBuilder.append(" required=\"true\">\n");
                } else {
                    stringBuilder.append(" penalty=\"").append(constraint.getPenalty()).append("\">\n");
                }

                indentation += INDENT_SIZE;
                for(String classId : constraint.getClassUnitIdList()) {
                    stringBuilder.append(addIndentation(indentation, "<class id=\"")).append(classId).append("\"/>\n");
                }
                indentation -= INDENT_SIZE;

                stringBuilder.append(addIndentation(indentation, "</distribution>\n"));
            }
            indentation -= INDENT_SIZE;
            stringBuilder.append(addIndentation(indentation, "</distributions>\n"));
        }

        stringBuilder.append(addIndentation(indentation, "<students/>\n"));

        stringBuilder.append("</problem>");

        writeToFile(file, stringBuilder.toString());
    }

    @Override
    public void exportToCSV(InMemoryRepository data) throws IOException {

    }

    @Override
    public void exportToPNG(InMemoryRepository data) throws IOException {

    }

    @Override
    public void exportToPDF(InMemoryRepository data) throws IOException {

    }
}
