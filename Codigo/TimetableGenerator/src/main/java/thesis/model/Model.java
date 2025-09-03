package thesis.model;

import thesis.controller.ControllerInterface;
import thesis.model.domain.InMemoryRepository;
import thesis.model.domain.elements.TableDisplayable;
import thesis.model.domain.elements.Timetable;
import thesis.model.exceptions.InvalidConfigurationException;
import thesis.model.exceptions.ParsingException;
import thesis.model.exporter.DataExporter;
import thesis.model.parser.InputFileReader;
import thesis.model.parser.XmlResult;
import thesis.solver.initialsolutiongenerator.InitialSolutionGenerator;
import thesis.solver.initialsolutiongenerator.MullerSolutionGenerator;
import thesis.solver.solutionoptimizer.HeuristicAlgorithm;
import thesis.solver.solutionoptimizer.SimulatedAnnealing;
import thesis.utils.DoubleToolkit;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class Model implements ModelInterface {
    private ControllerInterface controller;
    private final DataExporter dataExporter;
    private final InputFileReader inputFileReader;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), new DaemonThreadFactory());
    private final Map<String, InMemoryRepository> dataRepositoryHashMap = new ConcurrentHashMap<>();                            // ProgramName : Corresponding DataRepository
    private final ConcurrentMap<String, Future<Timetable>> generatedTimetables = new ConcurrentHashMap<>();                     // ProgramName : Timetable generated
    private final Map<String, InitialSolutionGenerator<Timetable>> initialSolutionGeneratorsMap = new ConcurrentHashMap<>();    // ProgramName : InitialSolutionGenerator
    private final Map<String, HeuristicAlgorithm<Timetable>> heuristicAlgorithmsMap = new ConcurrentHashMap<>();                // ProgramName : HeuristicAlgorithm

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
    public void importSolution(Timetable solution) throws InvalidConfigurationException {
        String program = solution.getProgramName();

        InMemoryRepository dataRepository = dataRepositoryHashMap.get(program);
        if(dataRepository == null) {
            throw new IllegalStateException("No data was already stored for the solution imported");
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
    public void startGeneratingSolution(String programName, Integer initSolutionMaxIter, double initialTemperature, double minTemperature, double coolingRate, int k) {
        InMemoryRepository data = controller.getDataRepository(programName);

        // Should never happen
        if(data == null) {
            throw new RuntimeException("The program name provided has no data");
        }

        // Clear any leftover data if there is any
        initialSolutionGeneratorsMap.remove(programName);
        heuristicAlgorithmsMap.remove(programName);

        // Pool the generation task
        generatedTimetables.put(programName, threadPool.submit(() -> generateTimetable(data, initSolutionMaxIter, initialTemperature, minTemperature, coolingRate, k)));
    }

    @Override
    public double getGenerationProgress(String programName) throws ExecutionException, InterruptedException, InvalidConfigurationException {
        Future<Timetable> taskResult = generatedTimetables.get(programName);

        if(taskResult == null) {
            throw new IllegalStateException("Can't return the progress because the process hasn't started yet");
        }

        // This throws an exception in case the thread crashed which can atleast provide an explanation of the
        // error, instead of failing silently
        try{
            taskResult.get(1, TimeUnit.MILLISECONDS);
        } catch (TimeoutException ignored) {} // This exception is expected as the timeout is extremely short. In this case can be ignored

        InitialSolutionGenerator<Timetable> initialSolutionGenerator = initialSolutionGeneratorsMap.get(programName);
        HeuristicAlgorithm<Timetable> heuristicAlgorithm = heuristicAlgorithmsMap.get(programName);

        // If the heuristicAlgorithm exists then the program has reached the 2nd phase of solution generation.
        // Otherwise its only in the 1st phase or hasn't even reached that point.
        if(heuristicAlgorithm != null) {
            double progress = 0.5 + heuristicAlgorithm.getProgress() / 2;

            if(DoubleToolkit.isEqual(progress, 1)) {
                Timetable timetable = generatedTimetables.get(programName).get();
                InMemoryRepository data = dataRepositoryHashMap.get(programName);
                data.addTimetable(timetable);
            }

            return progress;
        } else if(initialSolutionGenerator != null) {
            return initialSolutionGenerator.getProgress() / 2;
        } else {
            return 0;
        }
    }

    private Timetable generateTimetable(InMemoryRepository data, Integer initSolutionMaxIter, double initialTemperature, double minTemperature, double coolingRate, int k) {
        long start = System.currentTimeMillis();
        String programName = data.getProgramName();

        InitialSolutionGenerator<Timetable> initialSolutionGen = new MullerSolutionGenerator(data);
        initialSolutionGeneratorsMap.put(programName, initialSolutionGen);
        Timetable initialSolution = initialSolutionGen.generate(initSolutionMaxIter);

        System.out.println("Initial solution cost: " + initialSolution.cost());

        HeuristicAlgorithm<Timetable> heuristicAlgorithm = new SimulatedAnnealing(data, initialSolution, initialTemperature, minTemperature, coolingRate, k);
        heuristicAlgorithmsMap.put(programName, heuristicAlgorithm);
        Timetable generatedSolution = heuristicAlgorithm.execute();

        System.out.println("Optimized cost: " + generatedSolution.cost());

        // Runtime in seconds
        generatedSolution.setRuntime((System.currentTimeMillis() - start)/1000);

        return generatedSolution;
    }

    @Override
    public void cancelTimetableGeneration(String programName) {
        InitialSolutionGenerator<Timetable> initialSolutionGenerator = initialSolutionGeneratorsMap.get(programName);
        if(initialSolutionGenerator == null) return;
        initialSolutionGenerator.stopAlgorithm();

        HeuristicAlgorithm<Timetable> heuristicAlgorithm = heuristicAlgorithmsMap.get(programName);
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

    private static class DaemonThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setDaemon(true); // The thread is cancelled when the JVM is closed
            return thread;
        }
    }
}
