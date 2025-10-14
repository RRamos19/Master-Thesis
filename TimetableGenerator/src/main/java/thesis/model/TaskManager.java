package thesis.model;

import thesis.model.domain.InMemoryRepository;
import thesis.model.domain.components.Timetable;
import thesis.model.exceptions.InvalidConfigurationException;
import thesis.model.solver.core.DefaultISGSolution;
import thesis.model.solver.initialsolutiongenerator.InitialSolutionGenerator;
import thesis.model.solver.initialsolutiongenerator.MullerBasedSolutionGenerator;
import thesis.model.solver.solutionoptimizer.HeuristicAlgorithm;
import thesis.model.solver.solutionoptimizer.SimulatedAnnealing;
import thesis.utils.DaemonThreadFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public class TaskManager {
    private final ModelInterface model;
    private final ThreadMXBean bean = ManagementFactory.getThreadMXBean();
    private final Map<UUID, Future<Timetable>> generatedTimetables = new ConcurrentHashMap<>(); // ProgramUUID : Timetable generated
    private final Map<UUID, CountDownLatch> synchronizationMap = new ConcurrentHashMap<>();     // ProgramUUID : CountDownLatch (to synchronize the threads and perform cleanup safely)
    private final Map<UUID, TaskInformation> taskInformationMap = new ConcurrentHashMap<>();

    private final ExecutorService threadPool = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors(),
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
        taskInformationMap.put(progressUUID, new TaskInformation(data, bean));
        generatedTimetables.put(progressUUID, threadPool.submit(() -> generateTimetable(progressUUID, initialTemperature, minTemperature, coolingRate, k)));
    }

    public double getGenerationProgress(UUID progressUUID) throws ExecutionException, InterruptedException {
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

        TaskInformation taskInformation = taskInformationMap.get(progressUUID);
        CountDownLatch countDownLatch = synchronizationMap.get(progressUUID);

        if(taskInformation != null) {
            double progress = taskInformation.getGenerationProgress();

            if(progress >= 1 && countDownLatch != null) {
                countDownLatch.countDown();
            }

            return progress;
        } else {
            return 0;
        }
    }

    private Timetable generateTimetable(UUID progressUUID, double initialTemperature, double minTemperature, double coolingRate, int k) throws InvalidConfigurationException {
        TaskInformation taskInformation = taskInformationMap.get(progressUUID);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        synchronizationMap.put(progressUUID, countDownLatch);

        Timetable solution;
        try {
            solution = taskInformation.startGeneration(initialTemperature, minTemperature, coolingRate, k);
        } catch (InterruptedException e) {
            return null;
        }

        // The generation task was canceled (clear leftover data)
        if(solution == null) {
            synchronizationMap.remove(progressUUID);
            generatedTimetables.remove(progressUUID);
            taskInformationMap.remove(progressUUID);
            return null;
        }

        // Only locks if the final solution was generated
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Clear any leftover data
        generatedTimetables.remove(progressUUID);
        taskInformationMap.remove(progressUUID);

        return solution;
    }

    public void cancelTimetableGeneration(UUID progressUUID) {
        TaskInformation taskInformation = taskInformationMap.get(progressUUID);

        if(taskInformation != null) {
            taskInformation.cancelGeneration();
        }
    }

    private static class TaskInformation {
        private final ThreadMXBean bean;
        private final InMemoryRepository data;
        private final InitialSolutionGenerator<DefaultISGSolution> initialSolutionGenerator;
        private HeuristicAlgorithm<Timetable> heuristicAlgorithm;

        public TaskInformation(InMemoryRepository data, ThreadMXBean bean) {
            this.data = data;
            this.bean = bean;
            initialSolutionGenerator = new MullerBasedSolutionGenerator(data);
        }

        public double getGenerationProgress() {
            double progress = initialSolutionGenerator.getProgress();

            if(heuristicAlgorithm != null) {
                progress += heuristicAlgorithm.getProgress();
            }

            // The progress must be divided by 2 as the tasks have the same weight
            progress = progress / 2.0;

            // Fix the progress value (the usage of double can introduce error)
            // the progress is multiplied by 1000 for the final result to be a percentage with
            // one decimal place
            progress = Math.min(1, Math.round(progress * 1000) / 1000.0);

            return progress;
        }

        public Timetable startGeneration(double initialTemperature, double minTemperature, double coolingRate, int k) throws InterruptedException, InvalidConfigurationException {
            long id = Thread.currentThread().getId();
            long startCpu = bean.getThreadCpuTime(id); // Start time in nanoseconds

            DefaultISGSolution solution = initialSolutionGenerator.generate();

            // Generation was canceled
            if(solution == null) {
                return null;
            }

            heuristicAlgorithm = new SimulatedAnnealing(solution, initialTemperature, minTemperature, coolingRate, k);
            Timetable finalSolution = heuristicAlgorithm.execute();

            if(finalSolution != null) {
                finalSolution.setRuntime((bean.getThreadCpuTime(id) - startCpu)/1_000_000_000); // Runtime in seconds
                data.addTimetable(finalSolution);
            }

            return finalSolution;
        }

        public void cancelGeneration() {
            initialSolutionGenerator.stopAlgorithm();

            if(heuristicAlgorithm != null) {
                heuristicAlgorithm.stopAlgorithm();
            }
        }
    }
}
