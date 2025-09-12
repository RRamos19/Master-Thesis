package thesis.model;

import thesis.controller.ControllerInterface;
import thesis.model.domain.InMemoryRepository;
import thesis.model.domain.components.TableDisplayable;
import thesis.model.domain.components.Timetable;
import thesis.model.exceptions.CheckedIllegalStateException;
import thesis.model.exceptions.InvalidConfigurationException;
import thesis.model.exceptions.ParsingException;
import thesis.model.exporter.DataExporter;
import thesis.model.parser.InputFileReader;
import thesis.model.parser.XmlResult;
import thesis.solver.initialsolutiongenerator.InitialSolutionGenerator;
import thesis.solver.initialsolutiongenerator.MullerSolutionGenerator;
import thesis.solver.solutionoptimizer.HeuristicAlgorithm;
import thesis.solver.solutionoptimizer.SimulatedAnnealing;
import thesis.utils.DaemonThreadFactory;
import thesis.utils.DoubleToolkit;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class Model implements ModelInterface {
    private ControllerInterface controller;
    private final DataExporter dataExporter;
    private final InputFileReader inputFileReader;
    private final Map<String, InMemoryRepository> dataRepositoryHashMap = new ConcurrentHashMap<>();                            // ProgramName : Corresponding DataRepository
    private final ConcurrentMap<UUID, Future<Timetable>> generatedTimetables = new ConcurrentHashMap<>();                     // ProgramName : Timetable generated
    private final Map<UUID, InitialSolutionGenerator<Timetable>> initialSolutionGeneratorsMap = new ConcurrentHashMap<>();    // ProgramName : InitialSolutionGenerator
    private final Map<UUID, HeuristicAlgorithm<Timetable>> heuristicAlgorithmsMap = new ConcurrentHashMap<>();                // ProgramName : HeuristicAlgorithm
    private final Map<UUID, String> programGenerationMap = new HashMap<>();

    private final ExecutorService threadPool = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors(),
            new DaemonThreadFactory()
    );

    public Model(InputFileReader inputReader, DataExporter dataExporter) {
        this.inputFileReader = inputReader;
        this.dataExporter = dataExporter;
    }

    @Override
    public void setController(ControllerInterface controller) {
        this.controller = controller;
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
        InMemoryRepository data = controller.getDataRepository(programName);

        // Should never happen
        if(data == null) {
            throw new RuntimeException("The program name provided has no data");
        }

        programGenerationMap.put(progressUUID, programName);

        // Pool the generation task
        generatedTimetables.put(progressUUID, threadPool.submit(() -> generateTimetable(data, progressUUID, initSolutionMaxIter, initialTemperature, minTemperature, coolingRate, k)));
    }

    @Override
    public double getGenerationProgress(UUID progressUUID) throws ExecutionException, InterruptedException, InvalidConfigurationException {
        Future<Timetable> taskResult = generatedTimetables.get(progressUUID);

        if(taskResult == null) {
            throw new IllegalStateException("Can't return the progress because the process hasn't started yet");
        }

        // This throws an exception in case the thread crashed which can atleast provide an explanation of the
        // error, instead of failing silently
        try{
            taskResult.get(1, TimeUnit.NANOSECONDS);
        } catch (TimeoutException ignored) {
            // This exception is expected as the timeout is extremely short.
            // In this case it can be ignored
        }

        InitialSolutionGenerator<Timetable> initialSolutionGenerator = initialSolutionGeneratorsMap.get(progressUUID);
        HeuristicAlgorithm<Timetable> heuristicAlgorithm = heuristicAlgorithmsMap.get(progressUUID);

        // If the heuristicAlgorithm exists then the program has reached the 2nd phase of solution generation.
        // Otherwise its only in the 1st phase or hasn't even reached that point.
        if(heuristicAlgorithm != null) {
            double progress = 0.5 + heuristicAlgorithm.getProgress() / 2;

            // Generation has finished
            if(DoubleToolkit.isEqual(progress, 1)) {
                Timetable timetable = generatedTimetables.get(progressUUID).get();
                InMemoryRepository data = dataRepositoryHashMap.get(programGenerationMap.get(progressUUID));
                data.addTimetable(timetable);

                // Clear any leftover data if there is any
                initialSolutionGeneratorsMap.remove(progressUUID);
                heuristicAlgorithmsMap.remove(progressUUID);
                programGenerationMap.remove(progressUUID);
                generatedTimetables.remove(progressUUID);
            }

            return progress;
        } else if(initialSolutionGenerator != null) {
            return initialSolutionGenerator.getProgress() / 2;
        } else {
            return 0;
        }
    }

    private Timetable generateTimetable(InMemoryRepository data, UUID progressUUID, Integer initSolutionMaxIter, double initialTemperature, double minTemperature, double coolingRate, int k) {
        long start = System.currentTimeMillis();

        InitialSolutionGenerator<Timetable> initialSolutionGen = new MullerSolutionGenerator(data);
        initialSolutionGeneratorsMap.put(progressUUID, initialSolutionGen);
        Timetable initialSolution = initialSolutionGen.generate(initSolutionMaxIter);

        HeuristicAlgorithm<Timetable> heuristicAlgorithm = new SimulatedAnnealing(data, initialSolution, initialTemperature, minTemperature, coolingRate, k);
        heuristicAlgorithmsMap.put(progressUUID, heuristicAlgorithm);
        Timetable generatedSolution = heuristicAlgorithm.execute();

        generatedSolution.setRuntime((System.currentTimeMillis() - start)/1000); // Runtime in seconds

        return generatedSolution;
    }

    @Override
    public void cancelTimetableGeneration(UUID progressUUID) {
        InitialSolutionGenerator<Timetable> initialSolutionGenerator = initialSolutionGeneratorsMap.get(progressUUID);
        if(initialSolutionGenerator == null) return;
        initialSolutionGenerator.stopAlgorithm();

        HeuristicAlgorithm<Timetable> heuristicAlgorithm = heuristicAlgorithmsMap.get(progressUUID);
        if(heuristicAlgorithm == null) return;
        heuristicAlgorithm.stopAlgorithm();
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
