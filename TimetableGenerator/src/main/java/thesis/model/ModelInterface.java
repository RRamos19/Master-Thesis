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
import java.util.List;
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
    List<Course> getCourses(String progName) throws CheckedIllegalStateException;
    List<Config> getConfigs(String progName) throws CheckedIllegalStateException;
    List<Subpart> getSubparts(String progName) throws CheckedIllegalStateException;
    List<ClassUnit> getClassUnits(String progName) throws CheckedIllegalStateException;
    TimetableConfiguration getConfiguration(String progName) throws CheckedIllegalStateException;
    List<Constraint> getConstraints(String progName) throws CheckedIllegalStateException;
    List<Room> getRooms(String progName) throws CheckedIllegalStateException;
    List<Timetable> getTimetables(String progName) throws CheckedIllegalStateException;

    // Schedule solution generation methods
    void startGeneratingSolution(String programName, UUID progressUUID, double initialTemperature, double minTemperature, double coolingRate, int k);
    double getGenerationProgress(UUID progressUUID) throws ExecutionException, InterruptedException, InvalidConfigurationException;
    void cancelTimetableGeneration(UUID progressUUID);

    // Data import methods
    XmlResult readFile(File file) throws ParsingException, InvalidConfigurationException;
    void importRepository(InMemoryRepository repository);
    void importSolution(Timetable solution) throws InvalidConfigurationException, CheckedIllegalStateException;

    void removeTimetable(Timetable timetable);

    // Database connectivity
    void connectToDatabase(String ip, String port, String userName, String password) throws DatabaseException;
    void disconnectFromDatabase();

    // Data export methods
    String getExportLocation();
    void export(String programName, ExportType type) throws IOException;
}
