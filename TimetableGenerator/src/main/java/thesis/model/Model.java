package thesis.model;

import org.hibernate.HibernateException;
import thesis.controller.ControllerInterface;
import thesis.model.domain.InMemoryRepository;
import thesis.model.domain.components.TableDisplayable;
import thesis.model.domain.components.Timetable;
import thesis.model.exceptions.CheckedIllegalStateException;
import thesis.model.exceptions.DatabaseException;
import thesis.model.exceptions.InvalidConfigurationException;
import thesis.model.exceptions.ParsingException;
import thesis.model.exporter.DataExporter;
import thesis.model.parser.InputFileReader;
import thesis.model.parser.XmlResult;
import thesis.model.persistence.EntityRepository;
import thesis.model.repository.DBHibernateManager;
import thesis.model.repository.DBManager;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class Model implements ModelInterface {
    private static String DB_NAME = "";
    private ControllerInterface controller;
    private final DataExporter dataExporter;
    private final InputFileReader inputFileReader;
    private final Map<String, InMemoryRepository> dataRepositoryHashMap = new ConcurrentHashMap<>();                            // ProgramName : Corresponding DataRepository
    private final TaskManager taskManager;
    private DBManager<Collection<EntityRepository>> dbManager;

    public Model(InputFileReader inputReader, DataExporter dataExporter) {
        this.inputFileReader = inputReader;
        this.dataExporter = dataExporter;
        this.taskManager = new TaskManager(this);
    }

    @Override
    public void setController(ControllerInterface controller) {
        this.controller = controller;
    }

    @Override
    public void connectToDatabase(String ip, String port, String userName, String password) throws DatabaseException {
        dbManager = new DBHibernateManager(DB_NAME, ip, port, userName, password);
    }

    @Override
    public void disconnectFromDatabase() {
        dbManager = null;
    }

    @Override
    public XmlResult readFile(File file) throws ParsingException, InvalidConfigurationException {
        return inputFileReader.readXmlFile(file);
    }

    @Override
    public void importRepository(InMemoryRepository repository) {
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
    public Map<String, List<TableDisplayable>> getAllDisplayableData(String progName) {
        InMemoryRepository data = dataRepositoryHashMap.get(progName);

        Map<String, List<TableDisplayable>> result = new HashMap<>();

        if(data != null) {
            List<TableDisplayable> dataList = data.getAllDisplayableData();

            for(TableDisplayable obj : dataList) {
                List<TableDisplayable> resultObjectList = result.computeIfAbsent(obj.getTableName(), (s) -> new ArrayList<>());

                resultObjectList.add(obj);
            }
        }

        return result;
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
    public void startGeneratingSolution(String programName, UUID progressUUID, Integer initSolutionMaxIter, double initialTemperature, double minTemperature, double coolingRate, int k) {
        taskManager.startGeneratingSolution(programName, progressUUID, initSolutionMaxIter, initialTemperature, minTemperature, coolingRate, k);
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
