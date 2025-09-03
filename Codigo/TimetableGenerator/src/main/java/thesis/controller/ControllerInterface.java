package thesis.controller;

import thesis.model.ModelInterface;
import thesis.model.domain.InMemoryRepository;
import thesis.model.domain.elements.TableDisplayable;
import thesis.model.exceptions.InvalidConfigurationException;
import thesis.model.exceptions.ParsingException;
import thesis.view.ViewInterface;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public interface ControllerInterface {
    // Setters
    void setModel(ModelInterface model);
    void setView(ViewInterface view);

    // Getters
    InMemoryRepository getDataRepository(String programName);
    Set<String> getStoredPrograms();
    Map<String, List<TableDisplayable>> getAllDisplayableData(String progName);

    // Schedule solution generation methods
    void startGeneratingSolution(String programName, Integer initSolutionMaxIter, double initialTemperature, double minTemperature, double coolingRate, int k);
    double getGenerationProgress(String programName) throws InvalidConfigurationException, ExecutionException, InterruptedException;
    void cancelGeneration(String programName);

    // Data import methods
    void importITCData(File file);

    // Data export methods
    void exportSolutionsToITC(String programName);
    void exportDataToITC(String programName);
    void exportToCSV(String programName);
    void exportToPDF(String programName);
    void exportToPNG(String programName);
}
