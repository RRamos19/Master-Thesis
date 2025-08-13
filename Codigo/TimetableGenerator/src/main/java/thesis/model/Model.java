package thesis.model;

import thesis.controller.ControllerInterface;
import thesis.model.domain.DataRepository;
import thesis.model.domain.exceptions.ParsingException;
import thesis.model.exporter.DataExporter;
import thesis.model.exporter.TimetableDataExporter;
import thesis.model.parser.ITCFormatParser;
import thesis.model.parser.InputFileReader;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Model implements ModelInterface<DataRepository> {
    private ControllerInterface controller;
    private final Map<String, DataRepository> dataRepositoryHashMap = new HashMap<>(); // ProgramName : DataRepository
    private final DataExporter<DataRepository> dataExporter;
    private final InputFileReader<DataRepository> inputFileReader;

    public Model() {
        this.inputFileReader = new ITCFormatParser();
        this.dataExporter = new TimetableDataExporter();
    }

    public void setController(ControllerInterface controller) {
        this.controller = controller;
    }

    public void importITCData(File file) throws ParsingException {
        DataRepository dataRepository = inputFileReader.readFile(file);
        dataRepositoryHashMap.put(dataRepository.getProgramName(), dataRepository);
    }

    public void exportToCSV(String programName) throws IOException {
        DataRepository data = dataRepositoryHashMap.get(programName);

        if(data == null) {
            throw new RuntimeException("The ProgramName provided has no corresponding data");
        }

        dataExporter.exportToCSV(data);
    }

    public void exportToPDF(String programName) throws IOException {
        DataRepository data = dataRepositoryHashMap.get(programName);

        if(data == null) {
            throw new RuntimeException("The ProgramName provided has no corresponding data");
        }

        dataExporter.exportToPDF(data);
    }

    public void exportToPNG(String programName) throws IOException {
        DataRepository data = dataRepositoryHashMap.get(programName);

        if(data == null) {
            throw new RuntimeException("The ProgramName provided has no corresponding data");
        }

        dataExporter.exportToPNG(data);
    }

    public void exportDataToITC(String programName) throws IOException {
        DataRepository data = dataRepositoryHashMap.get(programName);

        if(data == null) {
            throw new RuntimeException("The ProgramName provided has no corresponding data");
        }

        dataExporter.exportDataToITC(data);
    }

    public void exportSolutionsToITC(String programName) throws IOException {
        DataRepository data = dataRepositoryHashMap.get(programName);

        if(data == null) {
            throw new RuntimeException("The ProgramName provided has no corresponding data");
        }

        dataExporter.exportSolutionsToITC(data);
    }
}
