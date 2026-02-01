package thesis.model.exporter;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thesis.model.XLSXFormat;
import thesis.model.domain.InMemoryRepository;
import thesis.model.domain.components.*;

import java.awt.Color;
import java.awt.Font;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName;

public class TimetableDataExporter implements DataExporter {
    private static final Logger logger = LoggerFactory.getLogger(TimetableDataExporter.class);
    private static final List<String> DAYS_OF_WEEK = List.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");
    private static final String EXPORT_LOCATION_PATH = "exports/";

    private final File exportLocation;
    private final int INDENT_SIZE = 2;
    private final Charset STANDARD_CHARSET = StandardCharsets.UTF_8;

    private static final Color LIGHT_GRAY_COLOR = new Color(210, 210, 210);
    private static final Color LIGHT_BLUE_COLOR = new Color(173, 216, 230);
    private static final Color LIGHT_GREEN_COLOR = new Color(156, 234, 157);

    public enum FileFormat {
        XML("xml"),
        XLSX("xlsx"),
        PDF("pdf"),
        PNG("png");

        private final String text;

        FileFormat(final String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    // XLSV creation variable
    String[] HEADERS = { "author", "title"};

    public TimetableDataExporter() {
        exportLocation = new File(EXPORT_LOCATION_PATH);

        if(!exportLocation.exists()) {
            if(!exportLocation.mkdir()) {
                throw new IllegalStateException("There was an error while creation a folder in path " + exportLocation.getAbsolutePath());
            }
        }
    }

    private File avoidFileOverwriting(String name, FileFormat fileFormat) throws IOException {
        int solutionExportNumber = 0;
        File file;
        do {
            // Checks if the file exists. If the answer is true then a
            // number between parenthesis is added to avoid overwriting an existing file
            String exportNumberString = solutionExportNumber >= 1 ? " (" + solutionExportNumber + ")" : "";
            file = new File(exportLocation.getAbsolutePath() + '/' + name + exportNumberString + '.' + fileFormat);
            solutionExportNumber++;
        } while(file.exists());

        if(file.createNewFile()) {
            return file;
        }

        throw new IOException("Couldn't create the following file: " + file.getAbsolutePath());
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

    public String getSolutionName(Timetable solution) {
        String fileName = "solution_" + solution.getProgramName();
        String filenameDateOfCreation = solution.getDateOfCreationString().replace(' ', '_').replace(':', '.');
        return fileName + '_' + filenameDateOfCreation;
    }

    @Override
    public void exportSolutionToITC(InMemoryRepository data, Timetable timetable) throws IOException {
        String originalDateOfCreation = timetable.getDateOfCreationString();
        File file = avoidFileOverwriting(getSolutionName(timetable), FileFormat.XML);
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("<solution xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"solution\" name=\"")
                .append(data.getProgramName())
                .append("\" runtime=\"").append(timetable.getRuntime())
                .append("\" timeStampUTC12=\"").append(originalDateOfCreation).append("\">\n");

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
            for(Map.Entry<Teacher, List<String>> teacherClass : teacherClassMap.entrySet()) {
                stringBuilder.append(addIndentation(indent, "<teacher id=\""))
                        .append(teacherClass.getKey().getId()).append("\">\n");

                indent += INDENT_SIZE;
                for(String classId : teacherClass.getValue()) {
                    stringBuilder.append(addIndentation(indent, "<class id=\""))
                            .append(classId)
                            .append("\"/>\n");
                }
                indent -= INDENT_SIZE;

                stringBuilder.append(addIndentation(indent, "</teacher>\n"));
            }
        }

        stringBuilder.append("</solution>");

        writeToFile(file, stringBuilder.toString());
    }

    @Override
    public void exportDataToITC(InMemoryRepository data) throws IOException {
        File file = avoidFileOverwriting(data.getProgramName(), FileFormat.XML);
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                .append("<!DOCTYPE problem PUBLIC \"-//ITC 2019//DTD Problem Format/EN\" \"http://www.itc2019.org/competition-format.dtd\">\n\n");

        TimetableConfiguration timetableConfiguration = data.getTimetableConfiguration();
        short numDays = timetableConfiguration.getNumDays();
        int numWeeks = timetableConfiguration.getNumWeeks();
        byte indentation = 0;

        // Problem configuration
        stringBuilder.append("<problem name=\"").append(data.getProgramName()).append("\" ")
                .append("nrDays=\"").append(timetableConfiguration.getNumDays()).append("\" ")
                .append("slotsPerDay=\"").append(timetableConfiguration.getSlotsPerDay()).append("\" ")
                .append("nrWeeks=\"").append(timetableConfiguration.getNumWeeks()).append("\">\n");

        indentation += INDENT_SIZE;

        // Problem weights
        stringBuilder.append(addIndentation(indentation, "<optimization time=\"")).append(timetableConfiguration.getTimeWeight()).append("\" ")
                .append("room=\"").append(timetableConfiguration.getRoomWeight()).append("\" ")
                .append("distribution=\"").append(timetableConfiguration.getDistribWeight()).append("\" ")
                .append("student=\"").append(0).append("\"/>\n");

        // Rooms
        Collection<Room> roomList = data.getRooms();
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
                    // If either of the subsections are empty then the tag is closed in a single line
                    stringBuilder.append("\"/>\n");
                }
            }
            indentation -= INDENT_SIZE;
            stringBuilder.append(addIndentation(indentation, "</rooms>\n"));
        }

        // Course, configs, subparts and classes
        Collection<Course> courseList = data.getCourses();
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
                            stringBuilder.append(addIndentation(indentation, "<class id=\"")).append(classUnit.getClassId()).append("\"");

                            String parentClass = classUnit.getParentClassId();
                            if(parentClass != null) {
                                stringBuilder.append(" parent=\"").append(parentClass).append("\"");
                            }

                            stringBuilder.append(">\n");

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
        Collection<Teacher> teacherList = data.getTeachers();
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
        Collection<Constraint> constraintList = data.getConstraints();
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

    private static Row nextXLSXRow(Sheet sheet) {
        return sheet.createRow(sheet.getLastRowNum() + 1);
    }

    private static void fillXLSXRow(Row row, Object... values) {
        for (int i = 0; i < values.length; i++) {
            Cell cell = row.createCell(i);
            Object value = values[i];

            if (value instanceof Number) {
                cell.setCellValue(((Number) value).doubleValue());
            } else if (value instanceof Boolean) {
                cell.setCellValue((Boolean) value);
            } else {
                cell.setCellValue(value != null ? value.toString() : "");
            }
        }
    }

    @Override
    public void exportDataToXLSX(InMemoryRepository data) throws IOException {
        File file = avoidFileOverwriting(data.getProgramName(), FileFormat.XLSX);

        try(XSSFWorkbook workbook = new XSSFWorkbook()) {
            for(Map.Entry<XLSXFormat.SHEET_NAMES, List<String>> sheetFormat : XLSXFormat.FORMAT_PER_SHEET.entrySet()) {
                XSSFSheet sheet = workbook.createSheet(sheetFormat.getKey().label);

                // Build the header of the sheet
                Row header = sheet.createRow(0);
                fillXLSXRow(header, sheetFormat.getValue().toArray());

                // Structures to help with the data exporting
                HashMap<String, Map<Time, Integer>> classTimes = new HashMap<>();
                HashMap<String, Map<String, Integer>> classRooms = new HashMap<>();
                HashMap<String, List<String>> classHierarchy = new HashMap<>();
                HashMap<String, List<Integer>> classTeachers = new HashMap<>();
                for(Course course : data.getCourses()) {
                    for(Config config : course.getConfigList()) {
                        for(Subpart subpart : config.getSubpartList()) {
                            for(ClassUnit classUnit : subpart.getClassUnitList()) {
                                String parentClass = classUnit.getParentClassId() != null ? classUnit.getParentClassId() : "";

                                classHierarchy.put(classUnit.getClassId(), List.of(
                                    course.getCourseId(),
                                    config.getConfigId(),
                                    subpart.getSubpartId(),
                                    classUnit.getClassId(),
                                    parentClass));

                                classRooms.put(classUnit.getClassId(), classUnit.getClassRoomPenalties());
                                classTimes.put(classUnit.getClassId(), classUnit.getClassTimePenalties());

                                List<Integer> teacherList = new ArrayList<>(classUnit.getTeacherIdList());
                                classTeachers.put(classUnit.getClassId(), teacherList);
                            }
                        }
                    }
                }

                // Fill the sheet with data
                switch(sheetFormat.getKey()) {
                    case Configuration:
                        {
                        Row row = nextXLSXRow(sheet);

                        TimetableConfiguration conf = data.getTimetableConfiguration();

                        fillXLSXRow(
                                row,
                                data.getProgramName(),
                                conf.getNumDays(),
                                conf.getSlotsPerDay(),
                                conf.getNumWeeks(),
                                conf.getTimeWeight(),
                                conf.getRoomWeight(),
                                conf.getDistribWeight());
                        }
                        break;
                    case Classes:
                        classHierarchy.forEach((classId, hierarchy) -> {
                            Row row = nextXLSXRow(sheet);

                            fillXLSXRow(row, hierarchy.toArray());
                        });
                        break;
                    case Class_Times:
                        classTimes.forEach((classId, times) -> {
                            for(Map.Entry<Time, Integer> timePenalty : times.entrySet()) {
                                Row row = nextXLSXRow(sheet);
                                Time time = timePenalty.getKey();

                                fillXLSXRow(
                                    row,
                                    classId,
                                    time.getStartSlot(),
                                    time.getLength(),
                                    time.getDays(),
                                    time.getWeeks(),
                                    timePenalty.getValue()
                                );
                            }
                        });
                        break;
                    case Class_Rooms:
                        classRooms.forEach((classId, rooms) -> {
                            for(Map.Entry<String, Integer> roomPenalty : rooms.entrySet()) {
                                Row row = nextXLSXRow(sheet);

                                fillXLSXRow(row, classId, roomPenalty.getKey(), roomPenalty.getValue());
                            }
                        });
                        break;
                    case Class_Teachers:
                        classTeachers.forEach((classId, teachers) -> {
                            for(int teacherId : teachers) {
                                Row row = nextXLSXRow(sheet);

                                fillXLSXRow(row, classId, teacherId);
                            }
                        });
                        break;
                    case Constraints:
                        for(Constraint constraint : data.getConstraints()) {
                            Row row = nextXLSXRow(sheet);

                            Integer penalty = constraint.getPenalty() != null ? constraint.getPenalty() : 0;

                            fillXLSXRow(row,
                                constraint.getId(),
                                constraint.getType(),
                                penalty,
                                constraint.getRequired(),
                                String.join(",", constraint.getClassUnitIdList()));
                        }
                        break;
                    case Teachers:
                        data.getTeachers().forEach((teacher) -> {
                            Row row = nextXLSXRow(sheet);

                            fillXLSXRow(row, teacher.getId(), teacher.getName());
                        });
                        break;
                    case Teacher_Unavailabilities:
                        data.getTeachers().forEach((teacher) -> {
                            for(Time unavailability : teacher.getTeacherUnavailabilities()) {
                                Row row = nextXLSXRow(sheet);

                                fillXLSXRow(
                                    row,
                                    teacher.getId(),
                                    unavailability.getStartSlot(),
                                    unavailability.getLength(),
                                    unavailability.getDays(),
                                    unavailability.getWeeks());
                            }
                        });
                        break;
                    case Rooms:
                        data.getRooms().forEach((room) -> {
                            Row row = nextXLSXRow(sheet);

                            fillXLSXRow(row, room.getRoomId());
                        });
                        break;
                    case Room_Unavailabilities:
                        data.getRooms().forEach((room) -> {
                            for(Time unavailability : room.getRoomUnavailabilities()) {
                                Row row = nextXLSXRow(sheet);

                                fillXLSXRow(
                                    row,
                                    room.getRoomId(),
                                    unavailability.getStartSlot(),
                                    unavailability.getLength(),
                                    unavailability.getDays(),
                                    unavailability.getWeeks());
                            }
                        });
                        break;
                    case Room_Distances:
                        data.getRooms().forEach((room1) -> {
                            room1.getRoomDistances().forEach((room2, penalty) -> {
                                Row row = nextXLSXRow(sheet);

                                fillXLSXRow(
                                    row,
                                    room1.getRoomId(),
                                    room2,
                                    penalty);
                            });
                        });
                        break;
                    case Solutions:
                        for(Timetable timetable : data.getTimetableList()) {
                            Row row = nextXLSXRow(sheet);

                            fillXLSXRow(
                                row,
                                timetable.getTimetableId(),
                                timetable.getRuntime(),
                                timetable.getDateOfCreationString());
                        }
                        break;
                    case Scheduled_Lessons:
                        for(Timetable timetable : data.getTimetableList()) {
                            for(ScheduledLesson lesson : timetable.getScheduledLessonList()) {
                                Row row = nextXLSXRow(sheet);

                                Time time = lesson.getScheduledTime();
                                String roomId = lesson.getRoomId() != null ? lesson.getRoomId() : "";

                                List<String> teacherIds = new ArrayList<>();
                                for(int teacherId : lesson.getTeacherIds()) {
                                    teacherIds.add(String.valueOf(teacherId));
                                }

                                fillXLSXRow(
                                    row,
                                    timetable.getTimetableId(),
                                    lesson.getClassId(),
                                    roomId,
                                    String.join(",", teacherIds),
                                    time.getStartSlot(),
                                    time.getLength(),
                                    time.getDays(),
                                    time.getWeeks());
                            }
                        }
                        break;
                    default:
                        throw new RuntimeException("XLSX Format Error!");
                }
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
        }
    }

    private Pair<Map<String, List<Integer>>, Map<String, List<ScheduledLesson>>> getWeekGroups(Timetable timetable, int maxWeeks) {
        Map<String, List<Integer>> weekGroups = new HashMap<>(); // key -> list of week numbers
        Map<String, List<ScheduledLesson>> lessonsByKey = new HashMap<>(); // key -> list of ScheduledLesson objects for that key

        for (int week = 0; week < maxWeeks; week++) {
            final int finalWeek = week;
            // collect active lessons this week
            List<ScheduledLesson> active = timetable.getScheduledLessonList().stream()
                .filter(l -> {
                    String weeks = l.getWeeksBinaryString();
                    return finalWeek < weeks.length() && weeks.charAt(finalWeek) == '1';
                })
                .collect(Collectors.toList());

            // create a key to represent all lessons
            List<String> ids = active.stream()
                .map(ScheduledLesson::getClassId)
                .sorted()
                .collect(Collectors.toList());

            String key = String.join("|", ids); // key creation

            if (weekGroups.containsKey(key)) {
                weekGroups.get(key).add(week + 1);
            } else {
                weekGroups.put(key, new ArrayList<>(List.of(week + 1)));
                lessonsByKey.put(key, active);
            }
        }

        return new ImmutablePair<>(weekGroups, lessonsByKey);
    }

    @Override
    public void exportSolutionToPNG(InMemoryRepository data, Timetable timetable, int maxHour, int minHour) throws IOException {
        int weeks = data.getTimetableConfiguration().getNumWeeks();

        Pair<Map<String, List<Integer>>, Map<String, List<ScheduledLesson>>> weeksAssignedLessons = getWeekGroups(timetable, weeks);
        Map<String, List<Integer>> mapGroups = weeksAssignedLessons.getLeft();
        Map<String, List<ScheduledLesson>> lessonGroups = weeksAssignedLessons.getRight();

        for(Map.Entry<String, List<Integer>> weekGroup : mapGroups.entrySet()) {
            List<String> weeksStringList = new ArrayList<>();
            weekGroup.getValue().forEach((week) -> weeksStringList.add(week.toString()));

            String weeksString = String.join("-", weeksStringList);
            File file = avoidFileOverwriting(getSolutionName(timetable) + "_weeks_" + weeksString, FileFormat.PNG);

            int days = data.getTimetableConfiguration().getNumDays();
            int numSlots = data.getTimetableConfiguration().getSlotsPerDay();
            int minutesPerSlot = 24 * 60 / numSlots;

            int startSlot = (minHour * 60) / minutesPerSlot;
            int endSlot = (maxHour * 60) / minutesPerSlot;

            int timeColWidth = 100;
            int cellWidth = 120;
            int cellHeight = 40;

            List<ScheduledLesson> assignedLessons = lessonGroups.get(weekGroup.getKey());

            // Group lessons by day
            Map<Integer, List<ScheduledLesson>> lessonsByDay = new HashMap<>();
            for (ScheduledLesson lesson : assignedLessons) {
                Time lessonTime = lesson.getScheduledTime();

                // Ignore lessons outside of start and end limits
                if(lessonTime.getStartSlot() < startSlot ||
                        lessonTime.getEndSlot() > endSlot) {
                    logger.warn("While exporting to PNG the lesson with id {} was ignored because it was outside the start and end hour limits", lesson.getClassId());
                    continue;
                }

                String mask = lesson.getDaysBinaryString();
                for (int day = 0; day < mask.length() && day < days; day++) {
                    if (mask.charAt(day) == '1') {
                        lessonsByDay.computeIfAbsent(day, k -> new ArrayList<>()).add(lesson);
                    }
                }
            }

            // For each day compute sub-column assignment
            Map<ScheduledLesson, Integer> subColAssignments = new HashMap<>();
            int[] subColsPerDay = new int[days];
            for (int day = 0; day < days; day++) {
                List<ScheduledLesson> dayList = lessonsByDay.getOrDefault(day, List.of())
                        .stream()
                        .sorted(Comparator.comparingInt(ScheduledLesson::getStartSlot))
                        .collect(Collectors.toList());

                List<Integer> lastSubCol = new ArrayList<>(); // end slot for each subcol
                for (ScheduledLesson lesson : dayList) {
                    int start = lesson.getStartSlot();
                    int end = lesson.getEndSlot();
                    int assignedSubColumn = -1;
                    for (int subcol = 0; subcol < lastSubCol.size(); subcol++) {
                        if (start >= lastSubCol.get(subcol)) {
                            assignedSubColumn = subcol;
                            lastSubCol.set(subcol, end);
                            break;
                        }
                    }
                    if (assignedSubColumn == -1) {
                        assignedSubColumn = lastSubCol.size();
                        lastSubCol.add(end);
                    }
                    subColAssignments.put(lesson, assignedSubColumn);
                }
                subColsPerDay[day] = Math.max(1, lastSubCol.size());
            }

            int totalSubColumns = Arrays.stream(subColsPerDay).sum();
            int width = timeColWidth + totalSubColumns * cellWidth;
            int height = (endSlot - startSlot + 1) * cellHeight;

            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();

            drawTimetableAWT(g, data, lessonsByDay, timeColWidth, cellWidth, cellHeight, minutesPerSlot, startSlot, endSlot, subColsPerDay, subColAssignments);

            g.dispose();
            ImageIO.write(image, FileFormat.PNG.toString(), file);
        }
    }

    @Override
    public void exportSolutionToPDF(InMemoryRepository data, Timetable timetable, int maxHour, int minHour) throws IOException {
        File file = avoidFileOverwriting(getSolutionName(timetable), FileFormat.PDF);

        TimetableConfiguration timetableConfiguration = data.getTimetableConfiguration();
        int days = timetableConfiguration.getNumDays();
        int weeks = timetableConfiguration.getNumWeeks();
        int numSlots = timetableConfiguration.getSlotsPerDay();
        int minutesPerSlot = 24 * 60 / numSlots;

        int startSlot = (minHour * 60) / minutesPerSlot;
        int endSlot = (maxHour * 60) / minutesPerSlot;

        int timeColWidth = 100;
        int cellWidth = 120;
        int cellHeight = 40;

        int height = (endSlot - startSlot + 1) * cellHeight;

        Pair<Map<String, List<Integer>>, Map<String, List<ScheduledLesson>>> weeksAssignedLessons = getWeekGroups(timetable, weeks);
        Map<String, List<Integer>> weekGroups = weeksAssignedLessons.getLeft();
        Map<String, List<ScheduledLesson>> lessonGroups = weeksAssignedLessons.getRight();

        try (PDDocument doc = new PDDocument()) {
            for(Map.Entry<String, List<Integer>> weekGroup : weekGroups.entrySet()) {
                List<ScheduledLesson> assignedLessons = lessonGroups.get(weekGroup.getKey());

                // Group lessons by day
                Map<Integer, List<ScheduledLesson>> lessonsByDay = new HashMap<>();
                for (ScheduledLesson lesson : assignedLessons) {
                    Time lessonTime = lesson.getScheduledTime();

                    // Ignore lessons outside of start and end limits
                    if(lessonTime.getStartSlot() < startSlot ||
                            lessonTime.getEndSlot() > endSlot) {
                        logger.warn("While exporting to PDF the lesson with id {} was ignored because it was outside the start and end hour limits", lesson.getClassId());
                        continue;
                    }

                    String mask = lesson.getDaysBinaryString();
                    for (int day = 0; day < mask.length() && day < days; day++) {
                        if (mask.charAt(day) == '1') {
                            lessonsByDay.computeIfAbsent(day, k -> new ArrayList<>()).add(lesson);
                        }
                    }
                }

                // For each day compute sub-column assignment
                Map<ScheduledLesson, Integer> subColAssignments = new HashMap<>();
                int[] subColsPerDay = new int[days];
                for (int day = 0; day < days; day++) {
                    List<ScheduledLesson> dayList = lessonsByDay.getOrDefault(day, List.of())
                            .stream()
                            .sorted(Comparator.comparingInt(ScheduledLesson::getStartSlot))
                            .collect(Collectors.toList());

                    List<Integer> lastSubCol = new ArrayList<>(); // end slot for each sub-column
                    for (ScheduledLesson lesson : dayList) {
                        int start = lesson.getStartSlot();
                        int end = lesson.getEndSlot();
                        int assignedSubColumn = -1;
                        for (int subcol = 0; subcol < lastSubCol.size(); subcol++) {
                            if (start >= lastSubCol.get(subcol)) {
                                assignedSubColumn = subcol;
                                lastSubCol.set(subcol, end);
                                break;
                            }
                        }
                        if (assignedSubColumn == -1) {
                            assignedSubColumn = lastSubCol.size();
                            lastSubCol.add(end);
                        }
                        subColAssignments.put(lesson, assignedSubColumn);
                    }
                    subColsPerDay[day] = Math.max(1, lastSubCol.size());
                }

                int totalSubColumns = Arrays.stream(subColsPerDay).sum();
                int width = timeColWidth + totalSubColumns * cellWidth;

                PDPage page = new PDPage(new PDRectangle(width, height));
                doc.addPage(page);

                try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                    drawTimetablePDF(cs, data, lessonsByDay, timeColWidth, cellWidth, cellHeight, minutesPerSlot, startSlot, endSlot, subColsPerDay, subColAssignments);
                }
            }

            doc.save(file);
        }
    }

    private static void drawTimetableAWT(Graphics2D g, InMemoryRepository data, Map<Integer, List<ScheduledLesson>> lessonsByDay,
                                         int timeColWidth, int cellWidth, int cellHeight,
                                         int minutesPerSlot, int startSlot, int endSlot,
                                         int[] subColsPerDay, Map<ScheduledLesson, Integer> subColAssignments) {

        int days = data.getTimetableConfiguration().getNumDays();
        int totalSubColumns = Arrays.stream(subColsPerDay).sum();
        int visibleSlots = endSlot - startSlot;

        // White Background
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, timeColWidth + totalSubColumns * cellWidth, (visibleSlots + 1) * cellHeight);

        // black colored grid
        g.setColor(Color.BLACK);
        for (int day = 0; day < days; day++) { // lines in y axis
            int dayColStart = 0;
            for (int dd = 0; dd < day; dd++) dayColStart += subColsPerDay[dd];

            for(int subCol = 0; subCol <= subColsPerDay[day]; subCol++) {
                int x = timeColWidth + (dayColStart + subCol) * cellWidth;
                g.drawLine(x, 0, x, (visibleSlots + 1) * cellHeight);
            }
        }
        for (int s = 0; s <= visibleSlots + 1; s++) { // lines in x axis
            int y = s * cellHeight;
            g.drawLine(0, y, timeColWidth + totalSubColumns * cellWidth, y);
        }

        // Headers
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        for (int day = 0; day < days; day++) {
            int dayColStart = 0;
            for (int dd = 0; dd < day; dd++) dayColStart += subColsPerDay[dd];

            int x = timeColWidth + dayColStart * cellWidth;
            int rectWidth = subColsPerDay[day] * cellWidth;

            Color columnColor = day % 2 == 0 ? Color.WHITE : Color.LIGHT_GRAY;

            g.setColor(columnColor);
            for(int cell = 0; cell <= visibleSlots; cell++) { // fill every cell in this column
                g.fillRect(x, cell * cellHeight, rectWidth, cellHeight);
            }
            g.setColor(Color.BLACK);
            g.drawRect(x, 0, rectWidth, cellHeight);
            drawCenteredString(g, DAYS_OF_WEEK.get(day), new Rectangle(x, 0, rectWidth, cellHeight));
        }

        // Time column
        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        for (int slot = startSlot; slot <= endSlot; slot++) {
            int y = (slot - startSlot + 1) * cellHeight;
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(0, y, timeColWidth, cellHeight);
            g.setColor(Color.BLACK);
            g.drawRect(0, y, timeColWidth, cellHeight);

            int startTime = slot * minutesPerSlot;
            int startHour = startTime / 60;
            int startMinutes = startTime % 60;

            int endTime = startTime + minutesPerSlot;
            int endHour = endTime / 60;
            int endMinutes = endTime % 60;
            String time = String.format("%02d:%02d - %02d:%02d", startHour, startMinutes, endHour, endMinutes);
            drawCenteredString(g, time, new Rectangle(0, y, timeColWidth, cellHeight));
        }

        // lessons
        g.setFont(new Font("SansSerif", Font.PLAIN, 10));
        for (int day = 0; day < days; day++) {
            List<ScheduledLesson> dayLessons = lessonsByDay.get(day);
            if(dayLessons == null) continue;

            Color cellColor = day % 2 == 0 ? LIGHT_BLUE_COLOR : LIGHT_GREEN_COLOR;

            for (ScheduledLesson lesson : dayLessons) {
                int subColIndex = subColAssignments.get(lesson);

                // compute column base for day
                int dayColStart = 0;
                for (int dd = 0; dd < day; dd++) dayColStart += subColsPerDay[dd];
                int targetCol = dayColStart + subColIndex;

                int x = timeColWidth + targetCol * cellWidth;
                int y = (lesson.getStartSlot() - startSlot + 1) * cellHeight;
                int height = lesson.getLength() * cellHeight;

                g.setColor(cellColor);
                g.fillRect(x, y, cellWidth, height);
                g.setColor(Color.BLACK);
                g.drawRect(x, y, cellWidth, height);

                List<String> teacherList = new ArrayList<>();
                for(Teacher teacher : lesson.getTeachers()) teacherList.add(teacher.getName());

                // Starting Y considering all the lines to be added
                int currentY = y - teacherList.size() * 15;
                drawCenteredString(g, lesson.getClassId(), new Rectangle(x, currentY, cellWidth, height));
                currentY += 15;
                drawCenteredString(g, "[" + data.getProgramName() + "]", new Rectangle(x, currentY, cellWidth, height));
                currentY += 15;
                // Write every name of every teacher who lectures this lesson
                for(int i=0; i<teacherList.size(); i++) {
                    String teacherName = teacherList.get(i);
                    String text = "";
                    if(i == 0) { // first element
                        text = "[(";
                    }

                    text += teacherName;

                    if (i == teacherList.size() - 1) { // last element
                        text += ")]";
                    } else {
                        text += ");";
                    }

                    drawCenteredString(g, text, new Rectangle(x, currentY, cellWidth, height));

                    currentY += 15;
                }
                drawCenteredString(g, "[" + lesson.getRoomId() + "]", new Rectangle(x, currentY, cellWidth, height));
            }
        }
    }

    private static float getPDFStringWidth(String text, PDType1Font font, int fontSize) throws IOException {
        return font.getStringWidth(text) / 1000 * fontSize;
    }

    private static void drawTimetablePDF(PDPageContentStream cs, InMemoryRepository data, Map<Integer, List<ScheduledLesson>> lessonsByDay,
                                         int timeColWidth, int cellWidth, int cellHeight,
                                         int minutesPerSlot, int startSlot, int endSlot,
                                         int[] subColsPerDay, Map<ScheduledLesson, Integer> subColAssignments) throws IOException {

        int days = data.getTimetableConfiguration().getNumDays();
        int totalSubColumns = Arrays.stream(subColsPerDay).sum();
        int visibleSlots = endSlot - startSlot;

        // White Background
        cs.setNonStrokingColor(Color.WHITE);
        cs.addRect(0, 0, timeColWidth + totalSubColumns * cellWidth, (visibleSlots + 1) * cellHeight);
        cs.fill();

        // Header
        cs.setFont(new PDType1Font(FontName.HELVETICA_BOLD), 10);
        for (int day = 0; day < days; day++) {
            int dayColStart = 0;
            for (int dd = 0; dd < day; dd++) dayColStart += subColsPerDay[dd];

            int x = timeColWidth + dayColStart * cellWidth;
            int y = visibleSlots * cellHeight;

            Color columnColor = day % 2 == 0 ? Color.WHITE : Color.LIGHT_GRAY;

            cs.setNonStrokingColor(columnColor);
            cs.addRect(x, 0, subColsPerDay[day] * cellWidth, visibleSlots * cellHeight);
            cs.fill();

            cs.setNonStrokingColor(Color.WHITE);
            cs.addRect(x, y, subColsPerDay[day] * cellWidth, cellHeight);
            cs.fill();

            cs.setStrokingColor(Color.BLACK);
            cs.addRect(x, y, subColsPerDay[day] * cellWidth, cellHeight);
            cs.stroke();

            // Day of week text
            cs.beginText();
            cs.setNonStrokingColor(Color.BLACK);
            cs.newLineAtOffset(x + (float) (subColsPerDay[day] * cellWidth) / 2 - 12, y + (float) cellHeight / 2 - 4);
            cs.showText(DAYS_OF_WEEK.get(day));
            cs.endText();
        }

        // black colored grid
        cs.setStrokingColor(Color.BLACK);
        for (int day = 0; day < days; day++) {
            int dayColStart = 0;
            for (int dd = 0; dd < day; dd++) dayColStart += subColsPerDay[dd];

            for(int subCol = 0; subCol <= subColsPerDay[day]; subCol++) {
                float x = timeColWidth + (dayColStart + subCol) * cellWidth;
                cs.moveTo(x, 0);
                cs.lineTo(x, visibleSlots * cellHeight);
            }
        }
        for (int slot = 0; slot <= visibleSlots + 1; slot++) {
            float y = slot * cellHeight;
            cs.moveTo(0, y);
            cs.lineTo(timeColWidth + totalSubColumns * cellWidth, y);
        }
        cs.stroke();

        // Hours column
        cs.setFont(new PDType1Font(FontName.HELVETICA), 11);
        {
            int x = timeColWidth / 2;
            for (int slot = visibleSlots; slot > 0; slot--) {
                int y = (slot - 1) * cellHeight;
                cs.setNonStrokingColor(Color.LIGHT_GRAY);
                cs.addRect(0, y, timeColWidth, cellHeight);
                cs.fill();

                cs.setNonStrokingColor(Color.BLACK);
                cs.addRect(0, y, timeColWidth, cellHeight);
                cs.stroke();

                int startTime = (endSlot - slot) * minutesPerSlot;
                int startHour = startTime / 60;
                int startMinutes = startTime % 60;

                int endTime = startTime + minutesPerSlot;
                int endHour = endTime / 60;
                int endMinutes = endTime % 60;
                String time = String.format("%02d:%02d - %02d:%02d", startHour, startMinutes, endHour, endMinutes);

                cs.beginText();
                cs.setNonStrokingColor(Color.BLACK);
                cs.newLineAtOffset(x - 30, y + (float) cellHeight / 2 - 4);
                cs.showText(time);
                cs.endText();
            }
        }

        // Lessons
        PDType1Font lessonFont = new PDType1Font(FontName.HELVETICA);
        int lessonFontSize = 9;
        cs.setFont(new PDType1Font(FontName.HELVETICA), lessonFontSize);
        for (int day = 0; day < days; day++) {
            List<ScheduledLesson> dayLessons = lessonsByDay.get(day);
            if(dayLessons == null) continue;

            Color cellColor = day % 2 == 0 ? LIGHT_BLUE_COLOR : LIGHT_GREEN_COLOR;

            for (ScheduledLesson lesson : dayLessons) {
                if (lesson.getDaysBinaryString().charAt(day) == '1') {
                    int subColIndex = subColAssignments.get(lesson);

                    int dayColStart = 0;
                    for (int dd = 0; dd < day; dd++) dayColStart += subColsPerDay[dd];
                    int targetCol = dayColStart + subColIndex;

                    float x = timeColWidth + targetCol * cellWidth;
                    float y = (endSlot - lesson.getStartSlot()) * cellHeight;
                    float height = lesson.getLength() * cellHeight;

                    cs.setNonStrokingColor(cellColor);
                    cs.addRect(x, y - height, cellWidth, height);
                    cs.fill();

                    cs.setStrokingColor(Color.BLACK);
                    cs.addRect(x, y - height, cellWidth, height);
                    cs.stroke();

                    List<String> teacherList = new ArrayList<>();
                    for(Teacher teacher : lesson.getTeachers()) teacherList.add(teacher.getName());

                    float textX = x + (float) cellWidth / 2;
                    float textY = y - height / 2;
                    if(!teacherList.isEmpty()) textY += 15 * (teacherList.size() - 1);

                    String line1 = lesson.getClassId();
                    String line2 = "[" + data.getProgramName() + "]";

                    String finalLine = "[" + lesson.getRoomId() + "]";
                    String finalTeacherLine = "";

                    float line1Width = getPDFStringWidth(line1, lessonFont, lessonFontSize);
                    float line2Width = getPDFStringWidth(line2, lessonFont, lessonFontSize);
                    float finalLineWidth = getPDFStringWidth(finalLine, lessonFont, lessonFontSize);

                    // Centered text
                    cs.beginText();
                    cs.setNonStrokingColor(Color.BLACK);
                    cs.newLineAtOffset(textX - line1Width / 2, textY); // Starting position
                    cs.showText(line1);
                    cs.newLineAtOffset((line1Width - line2Width) / 2, -15); // Offset from the previous line
                    cs.showText(line2);

                    float earlierLineWidth = line2Width;
                    for(int i=0; i<teacherList.size(); i++) {
                        String teacherName = teacherList.get(i);
                        String text = "";
                        if(i == 0) { // first element
                            text = "[(";
                        }

                        text += teacherName;

                        if (i == teacherList.size() - 1) { // last element
                            text += ")]";
                            finalTeacherLine = text;
                        } else {
                            text += ");";
                        }

                        float teacherLineWidth = getPDFStringWidth(text, lessonFont, lessonFontSize);

                        cs.newLineAtOffset((earlierLineWidth - teacherLineWidth) / 2, -15); // Offset from the previous line
                        cs.showText(text);
                        earlierLineWidth = teacherLineWidth;
                    }

                    float lastTeacherLineWidth = getPDFStringWidth(finalTeacherLine, lessonFont, lessonFontSize);
                    cs.newLineAtOffset((lastTeacherLineWidth - finalLineWidth) / 2, -15); // Offset from the previous line
                    cs.showText(finalLine);
                    cs.endText();
                }
            }
        }
    }

    // Centers the text in PNG
    private static void drawCenteredString(Graphics2D g, String text, Rectangle rect) {
        FontMetrics metrics = g.getFontMetrics(g.getFont());
        int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
        int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
        g.setColor(Color.BLACK);
        g.drawString(text, x, y);
    }
}
