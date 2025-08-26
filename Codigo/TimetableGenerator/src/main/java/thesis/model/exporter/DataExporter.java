package thesis.model.exporter;

import thesis.model.domain.InMemoryRepository;

import java.io.IOException;

public interface DataExporter {
    void exportSolutionsToITC(InMemoryRepository data) throws IOException;
    void exportDataToITC(InMemoryRepository data) throws IOException;
    void exportToCSV(InMemoryRepository data) throws IOException;
    void exportToPNG(InMemoryRepository data) throws IOException;
    void exportToPDF(InMemoryRepository data) throws IOException;
}
