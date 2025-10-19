package thesis.model;

import thesis.controller.ControllerInterface;
import thesis.model.domain.InMemoryRepository;
import thesis.model.domain.components.*;
import thesis.model.exceptions.CheckedIllegalStateException;
import thesis.model.exceptions.DatabaseException;
import thesis.model.exceptions.InvalidConfigurationException;
import thesis.model.exceptions.ParsingException;
import thesis.model.parser.XmlResult;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public interface ModelInterface {
    enum ExportType {
        CSV, PDF, PNG, DATA_ITC, SOLUTIONS_ITC
    }

    // Setters
    void setController(ControllerInterface controller);

    // Getters
    InMemoryRepository getDataRepository(String programName);
    Set<String> getStoredPrograms();
    Collection<Course> getCourses(String progName) throws CheckedIllegalStateException;
    Collection<Config> getConfigs(String progName) throws CheckedIllegalStateException;
    Collection<Subpart> getSubparts(String progName) throws CheckedIllegalStateException;
    Collection<ClassUnit> getClassUnits(String progName) throws CheckedIllegalStateException;
    TimetableConfiguration getConfiguration(String progName) throws CheckedIllegalStateException;
    Collection<Constraint> getConstraints(String progName) throws CheckedIllegalStateException;
    Collection<Room> getRooms(String progName) throws CheckedIllegalStateException;
    Collection<Timetable> getTimetables(String progName) throws CheckedIllegalStateException;

    // Schedule solution generation methods
    void startGeneratingSolution(String programName, UUID progressUUID, double initialTemperature, double minTemperature, double coolingRate, int k);
    double getGenerationProgress(UUID progressUUID) throws ExecutionException, InterruptedException, InvalidConfigurationException;
    void cancelTimetableGeneration(UUID progressUUID);

    // Data import methods
    XmlResult readFile(File file) throws ParsingException, InvalidConfigurationException;
    void importRepository(InMemoryRepository repository) throws InvalidConfigurationException;
    void importSolution(Timetable solution) throws InvalidConfigurationException, CheckedIllegalStateException;

    void removeTimetable(Timetable timetable);

    // Database connectivity
    void connectToDatabase(String ip, String port, String userName, String password) throws DatabaseException;
    void disconnectFromDatabase();

    // Data export methods
    String getExportLocation();
    void export(String programName, ExportType type) throws IOException;
}
