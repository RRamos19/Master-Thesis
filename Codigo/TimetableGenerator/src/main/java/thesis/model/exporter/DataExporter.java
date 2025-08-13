package thesis.model.exporter;

import thesis.model.domain.DataRepository;

import java.io.IOException;

public interface DataExporter<D> {
    void exportSolutionsToITC(D data) throws IOException;
    void exportDataToITC(DataRepository data) throws IOException;
    void exportToCSV(D data) throws IOException;
    void exportToPNG(D data) throws IOException;
    void exportToPDF(D data) throws IOException;
}
