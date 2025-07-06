package thesis.model.exporter;

import java.io.IOException;

public interface DataExporter<D> {
    void exportToITC(D data) throws IOException;
    void exportToCSV(D data, String fileName) throws IOException;
    void exportToPNG(D data, String fileName) throws IOException;
    void exportToPDF(D data, String fileName) throws IOException;
}
