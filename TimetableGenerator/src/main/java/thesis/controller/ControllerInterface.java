package thesis.controller;

import javafx.collections.ObservableList;
import thesis.model.ModelInterface;
import thesis.model.domain.InMemoryRepository;
import thesis.model.domain.components.Timetable;
import thesis.model.exceptions.InvalidConfigurationException;
import thesis.view.ViewInterface;
import thesis.view.viewobjects.*;

import java.io.File;
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
    ObservableList<ViewModel> getConfiguration(String progName);
    ObservableList<ViewModel> getCourses(String progName);
    ObservableList<ViewModel> getConfigs(String progName);
    ObservableList<ViewModel> getSubparts(String progName);
    ObservableList<ViewModel> getClassUnits(String progName);
    ObservableList<ViewModel> getConstraints(String progName);
    ObservableList<ViewModel> getRooms(String progName);
    ObservableList<ViewModel> getTimetables(String progName);

    // Schedule solution generation methods
    void startGeneratingSolution(String programName, UUID progressUUID, double initialTemperature, double minTemperature, double coolingRate, int k);
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
