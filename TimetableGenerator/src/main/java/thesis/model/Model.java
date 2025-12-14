package thesis.model;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thesis.controller.ControllerInterface;
import thesis.model.domain.InMemoryRepository;
import thesis.model.domain.components.*;
import thesis.model.exceptions.*;
import thesis.model.exporter.DataExporter;
import thesis.model.parser.InputFileReader;
import thesis.model.parser.XmlResult;
import thesis.model.persistence.DBHibernateManager;
import thesis.model.persistence.DBManager;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class Model implements ModelInterface {
    private static final Logger logger = LoggerFactory.getLogger(Model.class);
    private static final String DB_NAME = "timetabling_db";

    private ControllerInterface controller;
    private final DataExporter dataExporter;
    private final InputFileReader inputFileReader;
    private final Map<String, InMemoryRepository> dataRepositoryHashMap = new ConcurrentHashMap<>();                            // ProgramName : Corresponding DataRepository
    private final TaskManager taskManager;
    private DBManager<InMemoryRepository> dbManager;

    public Model(InputFileReader inputReader, DataExporter dataExporter) {
        this.inputFileReader = inputReader;
        this.dataExporter = dataExporter;
        this.taskManager = new TaskManager(this);
    }

    public ControllerInterface getController() {
        return controller;
    }

    @Override
    public void setController(ControllerInterface controller) {
        this.controller = controller;
    }

    @Override
    public void connectToDatabase(String ip, String port, String username, String password, int synchronizationTimeMinutes) throws DatabaseException {
        dbManager = new DBHibernateManager(DB_NAME, ip, port, username, password);
        taskManager.startSynchronizationTask(synchronizationTimeMinutes);
    }

    @Override
    public void fetchDataFromDatabase() throws InvalidConfigurationException {
        Collection<InMemoryRepository> inMemoryRepositories = dbManager.fetchData(dataRepositoryHashMap);

        Set<String> programConflicts = new HashSet<>();
        for(InMemoryRepository data : inMemoryRepositories) {
            if(dataRepositoryHashMap.containsKey(data.getProgramName())) {
                programConflicts.add(data.getProgramName());
            }
        }

        //TODO: change the confirmation

        if(!programConflicts.isEmpty()) {
            controller.showConfirmationAlert("When the fetching of data from the database was done, there were conflicts on the following programs: " + programConflicts + ". Overwrite ?", (confirmation) -> {
                for(InMemoryRepository data : inMemoryRepositories) {
                    // Only import if there are no conflicts, or, if there are, only overwrite if the user confirms.
                    if(!programConflicts.contains(data.getProgramName()) ||
                        programConflicts.contains(data.getProgramName()) && confirmation) {
                        try {
                            importRepository(data);
                        } catch (InvalidConfigurationException e) {
                            logger.error("Error ocurred while trying to import data!", e);
                            controller.showExceptionMessage(e);
                        }
                    }
                }

                controller.updateStoredPrograms();
                controller.updateTableView();
            });
        } else {
            for(InMemoryRepository data : inMemoryRepositories) {
                importRepository(data);
            }
        }

        controller.updateStoredPrograms();
        controller.updateTableView();
    }

    @Override
    public void storeInDatabase() {
        dbManager.storeData(dataRepositoryHashMap);
    }

    @Override
    public void disconnectFromDatabase() {
        taskManager.stopSynchronizationTask();
        dbManager = null;
    }

    @Override
    public void removeProgram(String program) {
        dataRepositoryHashMap.remove(program);

        // If there is a connection to a database remove the program from it as well
        if(dbManager != null) {
            controller.showConfirmationAlert("Remove the program from the database too ?", (confirmation) -> {
                if(confirmation) dbManager.removeProgram(program);
            });
        }
    }

    @Override
    public XmlResult readFile(File file) throws ParsingException, InvalidConfigurationException, IOException, CheckedIllegalArgumentException {
        return inputFileReader.readFile(file);
    }

    @Override
    public void importRepository(InMemoryRepository repository) throws InvalidConfigurationException {
        repository.cleanUnusedData();
        repository.verifyValidity();
        repository.setRoomBidirectionalDistances();
        dataRepositoryHashMap.put(repository.getProgramName(), repository);
    }

    @Override
    public String getExportLocation() {
        return dataExporter.getExportLocation();
    }

    @Override
    public void importSolution(Timetable solution) throws InvalidConfigurationException, CheckedIllegalStateException {
        String program = solution.getProgramName();

        InMemoryRepository dataRepository = dataRepositoryHashMap.get(program);
        if(dataRepository == null) {
            throw new CheckedIllegalStateException("No data was already stored for the solution imported");
        }

        dataRepository.addTimetable(solution);
    }

    @Override
    public InMemoryRepository getDataRepository(String programName) {
        return dataRepositoryHashMap.get(programName);
    }

    @Override
    public Set<String> getStoredPrograms() {
        return dataRepositoryHashMap.keySet();
    }

    @Override
    public Collection<Course> getCourses(String progName) throws CheckedIllegalStateException {
        InMemoryRepository dataRepository = dataRepositoryHashMap.get(progName);
        if(dataRepository == null) {
            throw new CheckedIllegalStateException("No data was already stored for the solution imported");
        }

        return dataRepository.getCourses();
    }

    @Override
    public Collection<ClassUnit> getClassUnits(String progName) throws CheckedIllegalStateException {
        InMemoryRepository dataRepository = dataRepositoryHashMap.get(progName);
        if(dataRepository == null) {
            throw new CheckedIllegalStateException("No data was already stored for the solution imported");
        }

        return dataRepository.getClassUnits();
    }

    @Override
    public TimetableConfiguration getConfiguration(String progName) throws CheckedIllegalStateException {
        InMemoryRepository dataRepository = dataRepositoryHashMap.get(progName);
        if(dataRepository == null) {
            throw new CheckedIllegalStateException("No data was already stored for the solution imported");
        }

        return dataRepository.getTimetableConfiguration();
    }

    @Override
    public Collection<Config> getConfigs(String progName) throws CheckedIllegalStateException {
        InMemoryRepository dataRepository = dataRepositoryHashMap.get(progName);
        if(dataRepository == null) {
            throw new CheckedIllegalStateException("No data was already stored for the solution imported");
        }

        return dataRepository.getConfigs();
    }

    @Override
    public Collection<Constraint> getConstraints(String progName) throws CheckedIllegalStateException {
        InMemoryRepository dataRepository = dataRepositoryHashMap.get(progName);
        if(dataRepository == null) {
            throw new CheckedIllegalStateException("No data was already stored for the solution imported");
        }

        return dataRepository.getConstraints();
    }

    @Override
    public Collection<Room> getRooms(String progName) throws CheckedIllegalStateException {
        InMemoryRepository dataRepository = dataRepositoryHashMap.get(progName);
        if(dataRepository == null) {
            throw new CheckedIllegalStateException("No data was already stored for the solution imported");
        }

        return dataRepository.getRooms();
    }

    @Override
    public Collection<Subpart> getSubparts(String progName) throws CheckedIllegalStateException {
        InMemoryRepository dataRepository = dataRepositoryHashMap.get(progName);
        if(dataRepository == null) {
            throw new CheckedIllegalStateException("No data was already stored for the solution imported");
        }

        return dataRepository.getSubparts();
    }

    @Override
    public Collection<Timetable> getTimetables(String progName) throws CheckedIllegalStateException {
        InMemoryRepository dataRepository = dataRepositoryHashMap.get(progName);
        if(dataRepository == null) {
            throw new CheckedIllegalStateException("No data was already stored for the solution imported");
        }

        return dataRepository.getTimetableList();
    }

    @Override
    public Collection<Teacher> getTeachers(String progName) throws CheckedIllegalStateException {
        InMemoryRepository dataRepository = dataRepositoryHashMap.get(progName);
        if(dataRepository == null) {
            throw new CheckedIllegalStateException("No data was already stored for the solution imported");
        }

        return dataRepository.getTeachers();
    }

    @Override
    public void startGeneratingSolution(String programName, UUID progressUUID, double initialTemperature, double minTemperature, double coolingRate, int k) {
        taskManager.startGeneratingSolution(programName, progressUUID, initialTemperature, minTemperature, coolingRate, k);
    }

    @Override
    public double getGenerationProgress(UUID progressUUID) throws ExecutionException, InterruptedException, InvalidConfigurationException {
        return taskManager.getGenerationProgress(progressUUID);
    }

    @Override
    public void cancelTimetableGeneration(UUID progressUUID) {
        taskManager.cancelTimetableGeneration(progressUUID);
    }

    @Override
    public void removeTimetable(Timetable timetable) {
        InMemoryRepository data = dataRepositoryHashMap.get(timetable.getProgramName());

        // If there is a connection to a database remove the solution from it as well
        if(dbManager != null) {
            controller.showConfirmationAlert("Remove the solution from the database too ?", (confirmation) -> {
                if(confirmation) dbManager.removeTimetable(timetable.getProgramName(), timetable.getTimetableId());
            });
        }

        data.removeTimetable(timetable);
    }

    @Override
    public void startReoptimizingSolution(Timetable timetable, UUID progressUUID, double initialTemperature, double minTemperature, double coolingRate, int k) {
        taskManager.startReoptimizingSolution(timetable, progressUUID, initialTemperature, minTemperature, coolingRate, k);
    }

    @Override
    public void export(String programName, ExportType type) throws IOException, InvalidFormatException {
        InMemoryRepository data = dataRepositoryHashMap.get(programName);
        if (data == null) {
            throw new RuntimeException("The program name provided has no corresponding data");
        }

        switch (type) {
            case XLSX:
                dataExporter.exportDataToXLSX(data);
                break;
            case DATA_ITC:
                dataExporter.exportDataToITC(data);
                break;
            default:
                throw new IllegalArgumentException("The export type provided couldn't be processed");
        }
    }

    @Override
    public void export(Timetable timetable, ExportType type) throws IOException {
        InMemoryRepository data = dataRepositoryHashMap.get(timetable.getProgramName());
        if (data == null) {
            throw new RuntimeException("The timetable provided has no corresponding data");
        }

        if (type == ExportType.SOLUTIONS_ITC) {
            dataExporter.exportSolutionToITC(data, timetable);
        } else {
            throw new IllegalArgumentException("The export type provided couldn't be processed");
        }
    }

    @Override
    public void export(Timetable timetable, int maxHour, int minHour, ExportType type) throws IOException {
        InMemoryRepository data = dataRepositoryHashMap.get(timetable.getProgramName());
        if (data == null) {
            throw new RuntimeException("The timetable provided has no corresponding data");
        }

        switch (type) {
            case PDF:
                dataExporter.exportSolutionToPDF(data, timetable, maxHour, minHour);
                break;
            case PNG:
                dataExporter.exportSolutionToPNG(data, timetable, maxHour, minHour);
                break;
            default:
                throw new IllegalArgumentException("The export type provided couldn't be processed");
        }
    }

    @Override
    public void cleanup() {
        taskManager.cleanup();
    }
}
