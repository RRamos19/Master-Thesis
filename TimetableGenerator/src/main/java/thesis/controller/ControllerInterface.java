package thesis.controller;

import thesis.model.ModelInterface;
import thesis.model.domain.InMemoryRepository;
import thesis.model.domain.components.TableDisplayable;
import thesis.model.domain.components.Timetable;
import thesis.model.exceptions.InvalidConfigurationException;
import thesis.view.ViewInterface;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public interface ControllerInterface {
    // Setters
    void setModel(ModelInterface model);
    void setView(ViewInterface view);

    // Getters
    InMemoryRepository getDataRepository(String programName);
    Set<String> getStoredPrograms();
    Map<String, List<TableDisplayable>> getAllDisplayableData(String progName);

    // Schedule solution generation methods
    void startGeneratingSolution(String programName, UUID progressUUID, Integer initSolutionMaxIter, double initialTemperature, double minTemperature, double coolingRate, int k);
    double getGenerationProgress(UUID progressUUID) throws InvalidConfigurationException, ExecutionException, InterruptedException;
    void cancelGeneration(UUID progressUUID);

    // Database connectivity
    void connectToDatabase(String ip, String port, String userName, String password) throws Exception;
    void disconnectFromDatabase();

    void removeTimetable(Timetable timetable);

    // Data import methods
    void importITCData(File file);

    // Data export methods
    void exportSolutionsToITC(String programName);
    void exportDataToITC(String programName);
    void exportToCSV(String programName);
    void exportToPDF(String programName);
    void exportToPNG(String programName);
}
