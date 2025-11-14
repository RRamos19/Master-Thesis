package thesis.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thesis.controller.ControllerInterface;
import thesis.model.domain.InMemoryRepository;
import thesis.model.domain.components.*;
import thesis.model.exceptions.CheckedIllegalStateException;
import thesis.model.exceptions.DatabaseException;
import thesis.model.exceptions.InvalidConfigurationException;
import thesis.model.exceptions.ParsingException;
import thesis.model.exporter.DataExporter;
import thesis.model.parser.InputFileReader;
import thesis.model.parser.XmlResult;
import thesis.model.persistence.DBManager;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class Model implements ModelInterface {
    private static final Logger logger = LoggerFactory.getLogger(Model.class);

    private ControllerInterface controller;
    private final DataExporter dataExporter;
    private final InputFileReader inputFileReader;
    private final Map<String, InMemoryRepository> dataRepositoryHashMap = new ConcurrentHashMap<>();                            // ProgramName : Corresponding DataRepository
    private final TaskManager taskManager;
    private final DBConnectionCache dbConnectionCache;
    private DBManager<InMemoryRepository> dbManager;

    public Model(InputFileReader inputReader, DataExporter dataExporter) {
        this.inputFileReader = inputReader;
        this.dataExporter = dataExporter;
        this.taskManager = new TaskManager(this);
        this.dbConnectionCache = new DBConnectionCache();
    }

    @Override
    public void setController(ControllerInterface controller) {
        this.controller = controller;
    }

    @Override
    public void connectToDatabase(String ip, String port, String userName, String password) throws DatabaseException {
        dbManager = dbConnectionCache.connectToDatabase(ip, port, userName, password);

        //TODO: just for testing
        try {
            fetchFromDatabase();
        } catch (Exception e) {
            dbConnectionCache.clearCache(ip, port);
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }

        storeInDatabase();
    }

    private void fetchFromDatabase() throws InvalidConfigurationException {
        Collection<InMemoryRepository> inMemoryRepositories = dbManager.fetchData(dataRepositoryHashMap);

        for(InMemoryRepository data : inMemoryRepositories) {
            importRepository(data);
        }

        controller.updateStoredPrograms();
        controller.updateTableView();
    }

    private void storeInDatabase() {
        //TODO: to be implemented
        //TODO: verify the last update timestamp
        dbManager.storeData(dataRepositoryHashMap);
    }

    @Override
    public void disconnectFromDatabase() {
        //TODO: remove auto fetch from database
    }

    @Override
    public void removeProgram(String program) {
        dataRepositoryHashMap.remove(program);
    }

    @Override
    public XmlResult readFile(File file) throws ParsingException, InvalidConfigurationException {
        return inputFileReader.readXmlFile(file);
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

    public TimetableConfiguration getConfiguration(String progName) throws CheckedIllegalStateException {
        InMemoryRepository dataRepository = dataRepositoryHashMap.get(progName);
        if(dataRepository == null) {
            throw new CheckedIllegalStateException("No data was already stored for the solution imported");
        }

        return dataRepository.getTimetableConfiguration();
    }

    public Collection<Config> getConfigs(String progName) throws CheckedIllegalStateException {
        InMemoryRepository dataRepository = dataRepositoryHashMap.get(progName);
        if(dataRepository == null) {
            throw new CheckedIllegalStateException("No data was already stored for the solution imported");
        }

        return dataRepository.getConfigs();
    }

    public Collection<Constraint> getConstraints(String progName) throws CheckedIllegalStateException {
        InMemoryRepository dataRepository = dataRepositoryHashMap.get(progName);
        if(dataRepository == null) {
            throw new CheckedIllegalStateException("No data was already stored for the solution imported");
        }

        return dataRepository.getConstraints();
    }

    public Collection<Room> getRooms(String progName) throws CheckedIllegalStateException {
        InMemoryRepository dataRepository = dataRepositoryHashMap.get(progName);
        if(dataRepository == null) {
            throw new CheckedIllegalStateException("No data was already stored for the solution imported");
        }

        return dataRepository.getRooms();
    }

    public Collection<Subpart> getSubparts(String progName) throws CheckedIllegalStateException {
        InMemoryRepository dataRepository = dataRepositoryHashMap.get(progName);
        if(dataRepository == null) {
            throw new CheckedIllegalStateException("No data was already stored for the solution imported");
        }

        return dataRepository.getSubparts();
    }

    public Collection<Timetable> getTimetables(String progName) throws CheckedIllegalStateException {
        InMemoryRepository dataRepository = dataRepositoryHashMap.get(progName);
        if(dataRepository == null) {
            throw new CheckedIllegalStateException("No data was already stored for the solution imported");
        }

        return dataRepository.getTimetableList();
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

        data.removeTimetable(timetable);
    }

    @Override
    public void export(String programName, ExportType type) throws IOException {
        InMemoryRepository data = dataRepositoryHashMap.get(programName);
        if (data == null) {
            throw new RuntimeException("The program name provided has no corresponding data");
        }

        switch (type) {
            case CSV:
                dataExporter.exportToCSV(data);
                break;
            case PDF:
                dataExporter.exportToPDF(data);
                break;
            case PNG:
                dataExporter.exportToPNG(data);
                break;
            case DATA_ITC:
                dataExporter.exportDataToITC(data);
                break;
            case SOLUTIONS_ITC:
                dataExporter.exportSolutionsToITC(data);
                break;
        }
    }
}
