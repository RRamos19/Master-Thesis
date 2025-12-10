package thesis.model.exporter;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import thesis.model.domain.InMemoryRepository;
import thesis.model.domain.components.Timetable;

import java.io.IOException;

public interface DataExporter {
    String getExportLocation();

    void exportSolutionToITC(InMemoryRepository data, Timetable timetable) throws IOException;
    void exportDataToITC(InMemoryRepository data) throws IOException;
    void exportDataToXLSX(InMemoryRepository data) throws IOException, InvalidFormatException;
    void exportSolutionToPNG(InMemoryRepository data, Timetable timetable, int maxHour, int minHour) throws IOException;
    void exportSolutionToPDF(InMemoryRepository data, Timetable timetable, int maxHour, int minHour) throws IOException;
}
