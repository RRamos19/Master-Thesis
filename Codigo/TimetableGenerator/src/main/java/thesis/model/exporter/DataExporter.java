package thesis.model.exporter;

import thesis.model.domain.DomainModel;

import java.io.IOException;

public interface DataExporter<D> {
    void exportSolutionsToITC(D data) throws IOException;
    void exportDomainToITC(DomainModel data) throws IOException;
    void exportToCSV(D data, String fileName) throws IOException;
    void exportToPNG(D data, String fileName) throws IOException;
    void exportToPDF(D data, String fileName) throws IOException;
}
