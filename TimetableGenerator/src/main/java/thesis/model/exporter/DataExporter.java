package thesis.model.exporter;

import thesis.model.domain.InMemoryRepository;
import thesis.model.domain.components.Timetable;

import java.io.IOException;

public interface DataExporter {
    String getExportLocation();

    void exportSolutionToITC(InMemoryRepository data, Timetable timetable) throws IOException;
    void exportDataToITC(InMemoryRepository data) throws IOException;
    void exportDataToCSV(InMemoryRepository data) throws IOException;
    void exportSolutionToPNG(InMemoryRepository data, Timetable timetable, int maxHour, int minHour) throws IOException;
    void exportSolutionToPDF(InMemoryRepository data, Timetable timetable, int maxHour, int minHour) throws IOException;
}
