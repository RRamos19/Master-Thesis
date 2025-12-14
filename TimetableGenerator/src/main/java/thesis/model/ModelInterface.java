package thesis.model;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import thesis.controller.ControllerInterface;
import thesis.model.domain.InMemoryRepository;
import thesis.model.domain.components.*;
import thesis.model.exceptions.*;
import thesis.model.parser.XmlResult;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public interface ModelInterface {
    enum ExportType {
        XLSX, PDF, PNG, DATA_ITC, SOLUTIONS_ITC
    }

    // Setters
    void setController(ControllerInterface controller);

    // Getters
    ControllerInterface getController();
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
    Collection<Teacher> getTeachers(String progName) throws CheckedIllegalStateException;

    // Schedule solution generation methods
    void startGeneratingSolution(String programName, UUID progressUUID, double initialTemperature, double minTemperature, double coolingRate, int k);
    void startReoptimizingSolution(Timetable timetable, UUID progressUUID, double initialTemperature, double minTemperature, double coolingRate, int k);
    double getGenerationProgress(UUID progressUUID) throws ExecutionException, InterruptedException, InvalidConfigurationException;
    void cancelTimetableGeneration(UUID progressUUID);

    // Data import methods
    XmlResult readFile(File file) throws ParsingException, InvalidConfigurationException, IOException, CheckedIllegalArgumentException;
    void importRepository(InMemoryRepository repository) throws InvalidConfigurationException;
    void importSolution(Timetable solution) throws InvalidConfigurationException, CheckedIllegalStateException;

    // Data removal methods
    void removeTimetable(Timetable timetable);
    void removeProgram(String program);

    // Database connectivity
    void connectToDatabase(String ip, String port, String userName, String password, int synchronizationTimeMinutes) throws DatabaseException;
    void disconnectFromDatabase();
    void fetchDataFromDatabase() throws InvalidConfigurationException;
    void storeInDatabase();

    // Data export methods
    String getExportLocation();
    void export(String programName, ExportType type) throws IOException, InvalidFormatException;
    void export(Timetable timetable, int maxHour, int minHour, ExportType type) throws IOException;
    void export(Timetable timetable, ExportType type) throws IOException;

    void cleanup();
}
