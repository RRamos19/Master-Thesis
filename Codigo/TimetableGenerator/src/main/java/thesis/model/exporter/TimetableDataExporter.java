package thesis.model.exporter;

import thesis.model.domain.DomainModel;
import thesis.model.domain.ScheduledLesson;
import thesis.model.domain.Timetable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimetableDataExporter implements DataExporter<DomainModel> {
    @Override
    public void exportToITC(DomainModel data) throws IOException {
        for(Timetable timetable : data.getTimetableList()) {
            File file = new File("solution_" + timetable.getProgram() + ".xml");
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            String formattedNow = now.format(formatter);
            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append("<solution xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"solution\" name=\"")
                    .append(data.getProblemName()).append("\" timeStampUTC12=\"")
                    .append(formattedNow)
                    .append("\">\n");

            for (ScheduledLesson scheduledLesson : timetable.getScheduledLessonList()) {
                stringBuilder.append("  <class id=\"").append(scheduledLesson.getClassId()).append("\"");

                if(scheduledLesson.getRoomId() != null) {
                    stringBuilder.append(" room=\"").append(scheduledLesson.getRoomId()).append("\"");
                }

                stringBuilder.append(" days=\"").append(scheduledLesson.getDaysBinaryString()).append("\"")
                        .append(" start=\"").append(scheduledLesson.getStartSlot()).append("\"")
                        .append(" weeks=\"").append(scheduledLesson.getWeeksBinaryString()).append("\"")
                        .append(" />\n");
            }

            stringBuilder.append("</solution>");

            try (FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write(stringBuilder.toString());
            }
        }
    }

    @Override
    public void exportToCSV(DomainModel data, String fileName) throws IOException {

    }

    @Override
    public void exportToPNG(DomainModel data, String fileName) throws IOException {

    }

    @Override
    public void exportToPDF(DomainModel data, String fileName) throws IOException {

    }
}
