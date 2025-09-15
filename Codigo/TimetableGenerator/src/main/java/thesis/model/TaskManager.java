package thesis.model;

import thesis.model.domain.InMemoryRepository;
import thesis.model.domain.components.Timetable;
import thesis.model.exceptions.InvalidConfigurationException;
import thesis.solver.initialsolutiongenerator.InitialSolutionGenerator;
import thesis.solver.initialsolutiongenerator.MullerSolutionGenerator;
import thesis.solver.solutionoptimizer.HeuristicAlgorithm;
import thesis.solver.solutionoptimizer.SimulatedAnnealing;
import thesis.utils.DaemonThreadFactory;
import thesis.utils.DoubleToolkit;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public class TaskManager {
    private final ModelInterface model;
    private final ThreadMXBean bean = ManagementFactory.getThreadMXBean();
    private final ConcurrentMap<UUID, Future<Timetable>> generatedTimetables = new ConcurrentHashMap<>();                     // ProgramName : Timetable generated
    private final Map<UUID, InitialSolutionGenerator<Timetable>> initialSolutionGeneratorsMap = new ConcurrentHashMap<>();    // ProgramName : InitialSolutionGenerator
    private final Map<UUID, HeuristicAlgorithm<Timetable>> heuristicAlgorithmsMap = new ConcurrentHashMap<>();                // ProgramName : HeuristicAlgorithm
    private final Map<UUID, String> programGenerationMap = new HashMap<>();
    private final ExecutorService threadPool = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors(),
            new DaemonThreadFactory()
    );

    public TaskManager(ModelInterface model) {
        this.model = model;
        bean.setThreadCpuTimeEnabled(true);
    }

    public void startGeneratingSolution(String programName, UUID progressUUID, Integer initSolutionMaxIter, double initialTemperature, double minTemperature, double coolingRate, int k) {
        InMemoryRepository data = model.getDataRepository(programName);

        // Should never happen
        if(data == null) {
            throw new RuntimeException("The program name provided has no data");
        }

        programGenerationMap.put(progressUUID, programName);

        // Pool the generation task
        generatedTimetables.put(progressUUID, threadPool.submit(() -> generateTimetable(data, progressUUID, initSolutionMaxIter, initialTemperature, minTemperature, coolingRate, k)));
    }

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
                InMemoryRepository data = model.getDataRepository(programGenerationMap.get(progressUUID));
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
        long id = Thread.currentThread().getId();
        long startCpu = bean.getThreadCpuTime(id); // Start time in nanoseconds

        InitialSolutionGenerator<Timetable> initialSolutionGen = new MullerSolutionGenerator(data);
        initialSolutionGeneratorsMap.put(progressUUID, initialSolutionGen);
        Timetable initialSolution = initialSolutionGen.generate(initSolutionMaxIter);

        HeuristicAlgorithm<Timetable> heuristicAlgorithm = new SimulatedAnnealing(data, initialSolution, initialTemperature, minTemperature, coolingRate, k);
        heuristicAlgorithmsMap.put(progressUUID, heuristicAlgorithm);
        Timetable generatedSolution = heuristicAlgorithm.execute();

        generatedSolution.setRuntime((bean.getThreadCpuTime(id) - startCpu)/1000000000); // Runtime in seconds

        return generatedSolution;
    }

    public void cancelTimetableGeneration(UUID progressUUID) {
        InitialSolutionGenerator<Timetable> initialSolutionGenerator = initialSolutionGeneratorsMap.get(progressUUID);
        if(initialSolutionGenerator == null) return;
        initialSolutionGenerator.stopAlgorithm();
        initialSolutionGeneratorsMap.remove(progressUUID);

        HeuristicAlgorithm<Timetable> heuristicAlgorithm = heuristicAlgorithmsMap.get(progressUUID);
        if(heuristicAlgorithm == null) return;
        heuristicAlgorithm.stopAlgorithm();
        heuristicAlgorithmsMap.remove(progressUUID);
    }
}
