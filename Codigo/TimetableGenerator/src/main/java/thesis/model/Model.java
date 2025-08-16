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
import java.util.Set;

public class Model implements ModelInterface<DataRepository> {
    private ControllerInterface controller;
    private final Map<String, DataRepository> dataRepositoryHashMap = new HashMap<>(); // ProgramName : DataRepository
    private final DataExporter<DataRepository> dataExporter;
    private final InputFileReader<DataRepository> inputFileReader;

    public Model() {
        this.inputFileReader = new ITCFormatParser();
        this.dataExporter = new TimetableDataExporter();
    }

    @Override
    public void setController(ControllerInterface controller) {
        this.controller = controller;
    }

    @Override
    public void importITCData(File file) throws ParsingException {
        DataRepository dataRepository = inputFileReader.readFile(file);
        dataRepositoryHashMap.put(dataRepository.getProgramName(), dataRepository);
    }

    @Override
    public DataRepository getDataRepository(String programName) {
        return dataRepositoryHashMap.get(programName);
    }

    @Override
    public Set<String> getStoredPrograms() {
        return dataRepositoryHashMap.keySet();
    }

    @Override
    public void exportToCSV(String programName) throws IOException {
        DataRepository data = dataRepositoryHashMap.get(programName);

        if(data == null) {
            throw new RuntimeException("The ProgramName provided has no corresponding data");
        }

        dataExporter.exportToCSV(data);
    }

    @Override
    public void exportToPDF(String programName) throws IOException {
        DataRepository data = dataRepositoryHashMap.get(programName);

        if(data == null) {
            throw new RuntimeException("The ProgramName provided has no corresponding data");
        }

        dataExporter.exportToPDF(data);
    }

    @Override
    public void exportToPNG(String programName) throws IOException {
        DataRepository data = dataRepositoryHashMap.get(programName);

        if(data == null) {
            throw new RuntimeException("The ProgramName provided has no corresponding data");
        }

        dataExporter.exportToPNG(data);
    }

    @Override
    public void exportDataToITC(String programName) throws IOException {
        DataRepository data = dataRepositoryHashMap.get(programName);

        if(data == null) {
            throw new RuntimeException("The ProgramName provided has no corresponding data");
        }

        dataExporter.exportDataToITC(data);
    }

    @Override
    public void exportSolutionsToITC(String programName) throws IOException {
        DataRepository data = dataRepositoryHashMap.get(programName);

        if(data == null) {
            throw new RuntimeException("The ProgramName provided has no corresponding data");
        }

        dataExporter.exportSolutionsToITC(data);
    }
}
