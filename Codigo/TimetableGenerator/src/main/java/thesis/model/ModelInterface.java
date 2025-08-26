package thesis.model;

import thesis.controller.ControllerInterface;
import thesis.model.domain.DataRepository;
import thesis.model.domain.InMemoryRepository;
import thesis.model.domain.elements.TableDisplayable;
import thesis.model.domain.elements.exceptions.ParsingException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public interface ModelInterface {
    // Setters
    void setController(ControllerInterface controller);

    // Getters
    InMemoryRepository getDataRepository(String programName);
    Set<String> getStoredPrograms();
    Map<String, List<TableDisplayable>> getAllDisplayableData(String progName);

    // Schedule solution generation methods
    void startGeneratingSolution(String programName, Integer initSolutionMaxIter, double initialTemperature, double minTemperature, double coolingRate, int k);
    double getGenerationProgress(String programName) throws ExecutionException, InterruptedException, ParsingException;
    void cancelTimetableGeneration(String programName);

    // Data import methods
    void importITCData(File file) throws ParsingException;

    // Data export methods
    void exportToCSV(String programName) throws IOException;
    void exportToPDF(String programName) throws IOException;
    void exportToPNG(String programName) throws IOException;
    void exportDataToITC(String programName) throws IOException;
    void exportSolutionsToITC(String programName) throws IOException;

    void cleanup() throws InterruptedException;
}
