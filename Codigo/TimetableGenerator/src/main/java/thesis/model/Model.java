package thesis.model;

import thesis.controller.ControllerInterface;
import thesis.model.domain.ClassUnit;
import thesis.model.domain.DataRepository;
import thesis.model.domain.Timetable;
import thesis.model.domain.exceptions.ParsingException;
import thesis.model.exporter.DataExporter;
import thesis.model.exporter.TimetableDataExporter;
import thesis.model.parser.ITCFormatParser;
import thesis.model.parser.InputFileReader;
import thesis.solver.initialsolutiongenerator.InitialSolutionGenerator;
import thesis.solver.initialsolutiongenerator.MullerSolutionGenerator;
import thesis.solver.solutionoptimizer.HeuristicAlgorithm;
import thesis.solver.solutionoptimizer.SimulatedAnnealing;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

public class Model implements ModelInterface<DataRepository> {
    private ControllerInterface controller;
    private final Map<String, DataRepository> dataRepositoryHashMap = new HashMap<>();                          // ProgramName : Corresponding DataRepository
    private final ConcurrentMap<String, Future<Timetable>> generatedTimetables = new ConcurrentHashMap<>();     // ProgramName : Timetable generated
    private final DataExporter<DataRepository> dataExporter;
    private final InputFileReader<DataRepository> inputFileReader;
    private final ExecutorService pool = Executors.newCachedThreadPool();
    private final Map<String, InitialSolutionGenerator<Timetable>> initialSolutionGeneratorsMap = new HashMap<>();
    private final Map<String, HeuristicAlgorithm<Timetable, ClassUnit>> heuristicAlgorithmsMap = new HashMap<>();

    public Model() {
        this.inputFileReader = new ITCFormatParser();
        this.dataExporter = new TimetableDataExporter();
    }

    @Override
    public void setController(ControllerInterface controller) {
        this.controller = controller;
    }

    @Override
    public void importITCData(File file) throws ParsingException {
        DataRepository dataRepository = inputFileReader.readFile(file);
        dataRepositoryHashMap.put(dataRepository.getProgramName(), dataRepository);
    }

    @Override
    public DataRepository getDataRepository(String programName) {
        return dataRepositoryHashMap.get(programName);
    }

    @Override
    public Set<String> getStoredPrograms() {
        return dataRepositoryHashMap.keySet();
    }

    @Override
    public void startGeneratingSolution(String programName, Integer initSolutionMaxIter, double initialTemperature, double minTemperature, double coolingRate, int k) {
        DataRepository data = controller.getDataRepository(programName);

        // Should never happen
        if(data == null) {
            throw new RuntimeException("The program name provided has no data");
        }

        // TODO: otimizar parâmetros e incluir parâmetros na janela de configuração
        generatedTimetables.put(programName, pool.submit(() -> generateTimetable(data, initSolutionMaxIter, initialTemperature, minTemperature, coolingRate, k)));
    }

    @Override
    public double getGenerationProgress(String programName) {
        if(generatedTimetables.get(programName) == null) {
            throw new IllegalStateException("Can't return the progress because the process hasn't started yet");
        }

        InitialSolutionGenerator<Timetable> initialSolutionGenerator = initialSolutionGeneratorsMap.get(programName);
        HeuristicAlgorithm<Timetable, ClassUnit> heuristicAlgorithm = heuristicAlgorithmsMap.get(programName);

        if(initialSolutionGenerator == null) {
            // Either the program hasn't started or has already finished (Most probable the latter)
            // TODO: confirmar
            return 1.0;
        }

        double progress = initialSolutionGenerator.getProgress() / 2;

        if(heuristicAlgorithm != null) {
            progress += heuristicAlgorithm.getProgress() / 2;
        }

        return progress;
    }

    @Override
    public Timetable getGeneratedTimetable(String programName) throws ExecutionException, InterruptedException {
        if(generatedTimetables.get(programName) == null) {
            throw new IllegalStateException("Can't return the result because the process hasn't started yet");
        }

        Timetable timetable = generatedTimetables.get(programName).get();
        DataRepository data = dataRepositoryHashMap.get(programName);
        data.addTimetable(timetable);

        return timetable;
    }

    private Timetable generateTimetable(DataRepository data, Integer initSolutionMaxIter, double initialTemperature, double minTemperature, double coolingRate, int k) {
        long start = System.currentTimeMillis();
        String programName = data.getProgramName();
        InitialSolutionGenerator<Timetable> initialSolutionGen = new MullerSolutionGenerator(data);
        initialSolutionGeneratorsMap.put(programName, initialSolutionGen);
        Timetable initialSolution = initialSolutionGen.generate(initSolutionMaxIter);

        HeuristicAlgorithm<Timetable, ClassUnit> heuristicAlgorithm = new SimulatedAnnealing(initialSolution, initialTemperature, minTemperature, coolingRate, k);
        heuristicAlgorithmsMap.put(programName, heuristicAlgorithm);
        Timetable generatedSolution = heuristicAlgorithm.execute();

        generatedSolution.setRuntime((System.currentTimeMillis() - start)/1000);

        initialSolutionGeneratorsMap.remove(programName);
        heuristicAlgorithmsMap.remove(programName);

        return generatedSolution;
    }

    @Override
    public void exportToCSV(String programName) throws IOException {
        DataRepository data = dataRepositoryHashMap.get(programName);

        if(data == null) {
            throw new RuntimeException("The ProgramName provided has no corresponding data");
        }

        dataExporter.exportToCSV(data);
    }

    @Override
    public void exportToPDF(String programName) throws IOException {
        DataRepository data = dataRepositoryHashMap.get(programName);

        if(data == null) {
            throw new RuntimeException("The ProgramName provided has no corresponding data");
        }

        dataExporter.exportToPDF(data);
    }

    @Override
    public void exportToPNG(String programName) throws IOException {
        DataRepository data = dataRepositoryHashMap.get(programName);

        if(data == null) {
            throw new RuntimeException("The ProgramName provided has no corresponding data");
        }

        dataExporter.exportToPNG(data);
    }

    @Override
    public void exportDataToITC(String programName) throws IOException {
        DataRepository data = dataRepositoryHashMap.get(programName);

        if(data == null) {
            throw new RuntimeException("The ProgramName provided has no corresponding data");
        }

        dataExporter.exportDataToITC(data);
    }

    @Override
    public void exportSolutionsToITC(String programName) throws IOException {
        DataRepository data = dataRepositoryHashMap.get(programName);

        if(data == null) {
            throw new RuntimeException("The ProgramName provided has no corresponding data");
        }

        dataExporter.exportSolutionsToITC(data);
    }

    public void cleanup() {
        // TODO: Confirmar se esta é a melhor opção
        pool.shutdownNow();
        generatedTimetables.forEach((s, r) -> r.cancel(true));
    }
}
