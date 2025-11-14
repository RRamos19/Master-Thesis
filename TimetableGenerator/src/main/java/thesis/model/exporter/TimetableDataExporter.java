package thesis.model.exporter;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import thesis.model.domain.InMemoryRepository;
import thesis.model.domain.components.*;

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
    private static final List<String> DAYS_OF_WEEK = List.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");
    private static final String EXPORT_LOCATION_PATH = "exports/";

    private final File exportLocation;
    private final int INDENT_SIZE = 2;
    private final Charset STANDARD_CHARSET = StandardCharsets.UTF_8;

    private static final Color LIGHT_GRAY_COLOR = new Color(210, 210, 210);
    private static final Color LIGHT_BLUE_COLOR = new Color(173, 216, 230);

    public enum FileFormat {
        XML("xml"),
        CSV("csv"),
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

    public TimetableDataExporter() {
        exportLocation = new File(EXPORT_LOCATION_PATH);

        if(!exportLocation.exists()) {
            if(!exportLocation.mkdir()) {
                throw new IllegalStateException("There was an error while creation a folder in path " + exportLocation.getAbsolutePath());
            }
        }
    }

    private File avoidFileOverwriting(String name, FileFormat fileFormat) {
        int solutionExportNumber = 0;
        File file;
        do {
            // Checks if the file exists. If the answer is true then a
            // number between parenthesis is added to avoid overwriting an existing file
            String exportNumberString = solutionExportNumber >= 1 ? " (" + solutionExportNumber + ")" : "";
            file = new File(exportLocation.getAbsolutePath() + '/' + name + exportNumberString + '.' + fileFormat);
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

    public String getSolutionName(Timetable solution) {
        String fileName = "solution_" + solution.getProgramName();
        String filenameDateOfCreation = solution.getDateOfCreationString().replace(' ', '_').replace(':', '.');
        return fileName + '_' + filenameDateOfCreation;
    }

    @Override
    public void exportSolutionsToITC(InMemoryRepository data) throws IOException {
        for(Timetable timetable : data.getTimetableList()) {
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

    @Override
    public void exportToCSV(InMemoryRepository data) throws IOException {

    }

    private Pair<Map<String, List<Integer>>, Map<String, List<ScheduledLesson>>> getWeekGroups(Timetable timetable, int maxWeeks) {
        // key -> list of week numbers
        LinkedHashMap<String, List<Integer>> weekGroups = new LinkedHashMap<>();
        // key -> list of ScheduledLesson objects for that key
        Map<String, List<ScheduledLesson>> lessonsByKey = new HashMap<>();

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
    public void exportToPNG(InMemoryRepository data) throws IOException {
        int weeks = data.getTimetableConfiguration().getNumWeeks();
        for(Timetable timetable : data.getTimetableList()) {
            Pair<Map<String, List<Integer>>, Map<String, List<ScheduledLesson>>> weeksAssignedLessons = getWeekGroups(timetable, weeks);
            Map<String, List<Integer>> mapGroups = weeksAssignedLessons.getLeft();
            Map<String, List<ScheduledLesson>> lessonGroups = weeksAssignedLessons.getRight();

            for(Map.Entry<String, List<Integer>> mapGroup : mapGroups.entrySet()) {
                List<String> weeksStringList = new ArrayList<>();
                mapGroup.getValue().forEach((week) -> weeksStringList.add(week.toString()));

                String weeksString = String.join("-", weeksStringList);
                File file = avoidFileOverwriting(getSolutionName(timetable) + "_weeks_" + weeksString, FileFormat.PNG);

                int days = data.getTimetableConfiguration().getNumDays();
                int numSlots = data.getTimetableConfiguration().getSlotsPerDay();
                int minutesPerSlot = 24 * 60 / numSlots;

                int timeColWidth = 100;
                int cellWidth = 120;
                int cellHeight = 40;

                int width = timeColWidth + days * cellWidth;
                int height = (numSlots + 1) * cellHeight;

                BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = image.createGraphics();

                drawTimetableAWT(g, data, lessonGroups.get(mapGroup.getKey()), timeColWidth, cellWidth, cellHeight, minutesPerSlot, numSlots);

                g.dispose();
                ImageIO.write(image, FileFormat.PNG.toString(), file);
            }
        }
    }

    @Override
    public void exportToPDF(InMemoryRepository data) throws IOException {
        for(Timetable timetable : data.getTimetableList()) {
            File file = avoidFileOverwriting(getSolutionName(timetable), FileFormat.PDF);

            TimetableConfiguration timetableConfiguration = data.getTimetableConfiguration();
            int days = timetableConfiguration.getNumDays();
            int weeks = timetableConfiguration.getNumWeeks();
            int numSlots = timetableConfiguration.getSlotsPerDay();
            int minutesPerSlot = 24 * 60 / numSlots;

            int timeColWidth = 100;
            int cellWidth = 120;
            int cellHeight = 40;

            int width = timeColWidth + days * cellWidth;
            int height = (numSlots + 1) * cellHeight;

            Pair<Map<String, List<Integer>>, Map<String, List<ScheduledLesson>>> weeksAssignedLessons = getWeekGroups(timetable, weeks);
            Map<String, List<Integer>> weekGroups = weeksAssignedLessons.getLeft();
            Map<String, List<ScheduledLesson>> lessonGroups = weeksAssignedLessons.getRight();

            try (PDDocument doc = new PDDocument()) {
                for(Map.Entry<String, List<Integer>> weekGroup : weekGroups.entrySet()) {
                    List<ScheduledLesson> assignedLessons = lessonGroups.get(weekGroup.getKey());

                    PDPage page = new PDPage(new PDRectangle(width, height));
                    doc.addPage(page);

                    try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                        drawTimetablePDF(cs, data, assignedLessons, timeColWidth, cellWidth, cellHeight, minutesPerSlot, numSlots);
                    }
                }

                doc.save(file);
            }
        }
    }

    private static void drawTimetableAWT(Graphics2D g, InMemoryRepository data, List<ScheduledLesson> assignedLessons,
                                         int timeColWidth, int cellWidth, int cellHeight,
                                         int minutesPerSlot, int visibleSlots) {

        int days = data.getTimetableConfiguration().getNumDays();

        // Background
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, timeColWidth + days * cellWidth, (visibleSlots + 1) * cellHeight);

        // Headers
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        for (int d = 0; d < days; d++) {
            int x = timeColWidth + d * cellWidth;
            g.setColor(Color.WHITE);
            g.fillRect(x, 0, cellWidth, cellHeight);
            g.setColor(Color.BLACK);
            g.drawRect(x, 0, cellWidth, cellHeight);
            drawCenteredString(g, DAYS_OF_WEEK.get(d), new Rectangle(x, 0, cellWidth, cellHeight));
        }

        // Time column
        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        for (int s = 0; s < visibleSlots; s++) {
            int y = (s + 1) * cellHeight;
            g.setColor(Color.WHITE);
            g.fillRect(0, y, timeColWidth, cellHeight);
            g.setColor(Color.BLACK);
            g.drawRect(0, y, timeColWidth, cellHeight);

            int startTime = s * minutesPerSlot;
            int startHour = startTime / 60;
            int startMinutes = startTime % 60;

            int endTime = startTime + minutesPerSlot;
            int endHour = endTime / 60;
            int endMinutes = endTime % 60;
            String time = String.format("%02d:%02d - %02d:%02d", startHour, startMinutes, endHour, endMinutes);
            drawCenteredString(g, time, new Rectangle(0, y, timeColWidth, cellHeight));
        }

        // light grey colored grid
        g.setColor(LIGHT_GRAY_COLOR);
        for (int d = 0; d <= days; d++) {
            int x = timeColWidth + d * cellWidth;
            g.drawLine(x, 0, x, (visibleSlots + 1) * cellHeight);
        }
        for (int s = 0; s <= visibleSlots + 1; s++) {
            int y = s * cellHeight;
            g.drawLine(0, y, timeColWidth + days * cellWidth, y);
        }

        // lessons
        g.setFont(new Font("SansSerif", Font.PLAIN, 10));
        for (ScheduledLesson l : assignedLessons) {
            for (int d = 0; d < days; d++) {
                if (l.getDaysBinaryString().charAt(d) == '1') {
                    int x = timeColWidth + d * cellWidth;
                    int y = (l.getStartSlot() + 1) * cellHeight;
                    int h = l.getLength() * cellHeight;

                    g.setColor(LIGHT_BLUE_COLOR);
                    g.fillRect(x, y, cellWidth, h);
                    g.setColor(Color.BLUE);
                    g.drawRect(x, y, cellWidth, h);

                    drawCenteredString(g, l.getClassId(), new Rectangle(x, y, cellWidth, h));
                }
            }
        }
    }

    private static void drawTimetablePDF(PDPageContentStream cs, InMemoryRepository data, List<ScheduledLesson> assignedLessons,
                                         int timeColWidth, int cellWidth, int cellHeight,
                                         int minutesPerSlot, int visibleSlots) throws IOException {

        int days = data.getTimetableConfiguration().getNumDays();

        // White Background
        cs.setNonStrokingColor(Color.WHITE);
        cs.addRect(0, 0, timeColWidth + days * cellWidth, (visibleSlots + 1) * cellHeight);
        cs.fill();

        // Header
        cs.setFont(new PDType1Font(FontName.HELVETICA_BOLD), 10);
        for (int d = 0; d < days; d++) {
            int x = timeColWidth + d * cellWidth;
            int y = visibleSlots * cellHeight;
            cs.setNonStrokingColor(Color.WHITE);
            cs.addRect(x, y, cellWidth, cellHeight);
            cs.fill();

            cs.setStrokingColor(Color.BLACK);
            cs.addRect(x, y, cellWidth, cellHeight);
            cs.stroke();

            // Day of week text
            cs.beginText();
            cs.setNonStrokingColor(Color.BLACK);
            cs.newLineAtOffset(x + (float) cellWidth / 2 - 12, y + (float) cellHeight / 2 - 4);
            cs.showText(DAYS_OF_WEEK.get(d));
            cs.endText();
        }

        // Hours column
        cs.setFont(new PDType1Font(FontName.HELVETICA), 11);
        {
            int x = timeColWidth / 2;
            for (int s = visibleSlots; s > 0; s--) {
                int y = (s - 1) * cellHeight;
                cs.setNonStrokingColor(Color.WHITE);
                cs.addRect(0, y, timeColWidth, cellHeight);
                cs.fill();

                cs.setNonStrokingColor(Color.BLACK);
                cs.addRect(0, y, timeColWidth, cellHeight);
                cs.stroke();

                int startTime = (visibleSlots - s) * minutesPerSlot;
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

        // Light grey colored grid
        cs.setStrokingColor(LIGHT_GRAY_COLOR);
        for (int d = 0; d <= days; d++) {
            float x = timeColWidth + d * cellWidth;
            cs.moveTo(x, 0);
            cs.lineTo(x, (visibleSlots + 1) * cellHeight);
        }
        for (int s = 0; s <= visibleSlots + 1; s++) {
            float y = s * cellHeight;
            cs.moveTo(0, y);
            cs.lineTo(timeColWidth + days * cellWidth, y);
        }
        cs.stroke();

        // Lessons
        cs.setFont(new PDType1Font(FontName.HELVETICA), 9);
        for (ScheduledLesson l : assignedLessons) {
            for (int d = 0; d < days; d++) {
                if (l.getDaysBinaryString().charAt(d) == '1') {
                    float x = timeColWidth + d * cellWidth;
                    float y = (visibleSlots - l.getStartSlot()) * cellHeight;
                    float h = l.getLength() * cellHeight;

                    cs.setNonStrokingColor(LIGHT_BLUE_COLOR);
                    cs.addRect(x, y - h, cellWidth, h);
                    cs.fill();

                    cs.setStrokingColor(Color.BLUE);
                    cs.addRect(x, y - h, cellWidth, h);
                    cs.stroke();

                    // Centered text
                    cs.beginText();
                    cs.setNonStrokingColor(Color.BLACK);
                    cs.newLineAtOffset(x + 10, y - h / 2);
                    cs.showText(l.getClassId());
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
