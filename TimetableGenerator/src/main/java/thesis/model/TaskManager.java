package thesis.model;

import thesis.model.domain.InMemoryRepository;
import thesis.model.domain.components.Timetable;
import thesis.model.exceptions.InvalidConfigurationException;
import thesis.solver.core.DefaultISGSolution;
import thesis.solver.initialsolutiongenerator.InitialSolutionGenerator;
import thesis.solver.initialsolutiongenerator.MullerSolutionGenerator;
import thesis.solver.solutionoptimizer.HeuristicAlgorithm;
import thesis.solver.solutionoptimizer.SimulatedAnnealing;
import thesis.utils.DaemonThreadFactory;
import thesis.utils.DoubleToolkit;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public class TaskManager {
    private final ModelInterface model;
    private final ThreadMXBean bean = ManagementFactory.getThreadMXBean();
    private final ConcurrentMap<UUID, Future<Timetable>> generatedTimetables = new ConcurrentHashMap<>();                              // ProgramUUID : Timetable generated
    private final Map<UUID, InitialSolutionGenerator<DefaultISGSolution>> initialSolutionGeneratorsMap = new ConcurrentHashMap<>();    // ProgramUUID : InitialSolutionGenerator
    private final Map<UUID, HeuristicAlgorithm<Timetable>> heuristicAlgorithmsMap = new ConcurrentHashMap<>();                         // ProgramUUID : HeuristicAlgorithm
    private final Map<UUID, CountDownLatch> synchronizationMap = new ConcurrentHashMap<>();                                            // ProgramUUID : CountDownLatch (to synchronize the threads and perform cleanup safely)
    private final ExecutorService threadPool = Executors.newFixedThreadPool(
            2,//Runtime.getRuntime().availableProcessors(),
            new DaemonThreadFactory()
    );

    public TaskManager(ModelInterface model) {
        this.model = model;
        bean.setThreadCpuTimeEnabled(true);
    }

    public void startGeneratingSolution(String programName, UUID progressUUID, double initialTemperature, double minTemperature, double coolingRate, int k) {
        InMemoryRepository data = model.getDataRepository(programName);

        // Should never happen
        if(data == null) {
            throw new RuntimeException("The program name provided has no data");
        }

        // Pool the generation task
        generatedTimetables.put(progressUUID, threadPool.submit(() -> generateTimetable(data, progressUUID, initialTemperature, minTemperature, coolingRate, k)));
    }

    public double getGenerationProgress(UUID progressUUID) throws ExecutionException, InterruptedException, InvalidConfigurationException {
        Future<Timetable> taskResult = generatedTimetables.get(progressUUID);

        if(taskResult == null) {
            throw new IllegalStateException("Can't return the progress because the process hasn't started yet");
        }

        // This throws an exception in case the thread crashed which can at least provide an explanation of the
        // error, instead of failing silently
        try{
            taskResult.get(1, TimeUnit.NANOSECONDS);
        } catch (TimeoutException ignored) {
            // This exception is expected as the timeout is extremely short.
            // In this case it can be ignored
        }

        InitialSolutionGenerator<DefaultISGSolution> initialSolutionGenerator = initialSolutionGeneratorsMap.get(progressUUID);
        HeuristicAlgorithm<Timetable> heuristicAlgorithm = heuristicAlgorithmsMap.get(progressUUID);

        // If the heuristicAlgorithm exists then the program has reached the 2nd phase of solution generation.
        // Otherwise, its only in the 1st phase or hasn't even reached that point.
        if(heuristicAlgorithm != null) {
            double progress = 0.5 + heuristicAlgorithm.getProgress() / 2;

            if(DoubleToolkit.isEqual(progress, 1)) {
                CountDownLatch countDownLatch = synchronizationMap.get(progressUUID);
                if(countDownLatch != null) {
                    countDownLatch.countDown();
                }
            }

            return progress;
        } else if(initialSolutionGenerator != null) {
            return initialSolutionGenerator.getProgress() / 2;
        } else {
            return 0;
        }
    }

    private Timetable generateTimetable(InMemoryRepository data, UUID progressUUID, double initialTemperature, double minTemperature, double coolingRate, int k) throws InvalidConfigurationException {
        long id = Thread.currentThread().getId();
        long startCpu = bean.getThreadCpuTime(id); // Start time in nanoseconds

        CountDownLatch countDownLatch = new CountDownLatch(1);
        synchronizationMap.put(progressUUID, countDownLatch);

        InitialSolutionGenerator<DefaultISGSolution> initialSolutionGen = new MullerSolutionGenerator(data);
        initialSolutionGeneratorsMap.put(progressUUID, initialSolutionGen);
        DefaultISGSolution initialSolution = initialSolutionGen.generate();

        // The generation task was canceled
        if(initialSolution == null) {
            initialSolutionGeneratorsMap.remove(progressUUID);
            synchronizationMap.remove(progressUUID);
            generatedTimetables.remove(progressUUID);
            return null;
        }

        HeuristicAlgorithm<Timetable> heuristicAlgorithm = new SimulatedAnnealing(initialSolution, initialTemperature, minTemperature, coolingRate, k);
        heuristicAlgorithmsMap.put(progressUUID, heuristicAlgorithm);
        Timetable generatedSolution = heuristicAlgorithm.execute();

        if(generatedSolution != null) {
            generatedSolution.setRuntime((bean.getThreadCpuTime(id) - startCpu)/1_000_000_000); // Runtime in seconds
            data.addTimetable(generatedSolution);

            // Only locks if the final solution was generated
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        // Clear any leftover data
        generatedTimetables.remove(progressUUID);
        initialSolutionGeneratorsMap.remove(progressUUID);
        heuristicAlgorithmsMap.remove(progressUUID);
        synchronizationMap.remove(progressUUID);

        return generatedSolution;
    }

    public void cancelTimetableGeneration(UUID progressUUID) {
        InitialSolutionGenerator<DefaultISGSolution> initialSolutionGenerator = initialSolutionGeneratorsMap.get(progressUUID);
        HeuristicAlgorithm<Timetable> heuristicAlgorithm = heuristicAlgorithmsMap.get(progressUUID);

        if(initialSolutionGenerator != null) {
            initialSolutionGenerator.stopAlgorithm();
        }

        if(heuristicAlgorithm != null) {
            heuristicAlgorithm.stopAlgorithm();
        }
    }
}
