package thesis.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thesis.controller.ControllerInterface;
import thesis.model.domain.InMemoryRepository;
import thesis.model.domain.components.ClassUnit;
import thesis.model.domain.components.ScheduledLesson;
import thesis.model.domain.components.Timetable;
import thesis.model.exceptions.InvalidConfigurationException;
import thesis.model.solver.core.DefaultISGSolution;
import thesis.model.solver.core.DefaultISGValue;
import thesis.model.solver.core.DefaultISGVariable;
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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class is in charge of the creation and maintenance of the threads used by the system.
 * The threads may be for the generation of solutions or the synchronization with the database.
 */
public class TaskManager {
    private static final Logger logger = LoggerFactory.getLogger(TaskManager.class);

    private final ModelInterface model;
    private final ThreadMXBean bean = ManagementFactory.getThreadMXBean();
    private final Map<UUID, Future<Timetable>> generatedTimetables = new ConcurrentHashMap<>(); // ProgramUUID : Timetable generated
    private final Map<UUID, CountDownLatch> synchronizationMap = new ConcurrentHashMap<>();     // ProgramUUID : CountDownLatch (to synchronize the threads and perform cleanup safely)
    private final Map<UUID, TaskInformation> taskInformationMap = new ConcurrentHashMap<>();

    private final ExecutorService threadPool = Executors.newFixedThreadPool(
            2, //Runtime.getRuntime().availableProcessors(),
            new DaemonThreadFactory()
    );

    private final ScheduledExecutorService synchronizationExecutorService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> synchronizationTask;
    private final AtomicInteger numGenerationTasks = new AtomicInteger(0);
    private static final int BLOCK_SLEEP_TIME = 1000 * 60;

    public TaskManager(ModelInterface model) {
        this.model = model;
        bean.setThreadCpuTimeEnabled(true);
    }

    public void startSynchronizationTask(int timeInMinutes) {
        logger.info("Starting Auto Synchronization Task!");

        synchronizationTask = synchronizationExecutorService.scheduleAtFixedRate(() -> {
            try {
                while (numGenerationTasks.get() != 0) {
                    try {
                        Thread.sleep(BLOCK_SLEEP_TIME);
                    } catch (InterruptedException ignored) {}
                }

                if (Thread.interrupted()) return;

                logger.info("Initiating Synchronization!");

                try {
                    model.fetchDataFromDatabase();
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    ControllerInterface controller = model.getController();
                    if (controller != null) {
                        controller.showExceptionMessage(e);
                    }
                }

                model.storeInDatabase();

                logger.info("Synchronization Finished!");
            } catch (Exception e) {
                logger.error(e.getMessage());
                ControllerInterface controller = model.getController();
                if (controller != null) {
                    controller.showExceptionMessage(e);
                }
            }
        }, 0, timeInMinutes, TimeUnit.MINUTES);
    }

    public void stopSynchronizationTask() {
        synchronizationTask.cancel(true);
        logger.info("Synchronization Cancelled!");
    }

    public void startGeneratingSolution(String programName, UUID progressUUID, double initialTemperature, double minTemperature, double coolingRate, int k) {
        InMemoryRepository data = model.getDataRepository(programName);

        // Should never happen
        if(data == null) {
            throw new RuntimeException("The program name provided has no data");
        }
        if(data.getRooms().isEmpty()) {
            throw new RuntimeException("There are no rooms stored in this problem, impossible to generate a timetable");
        }

        // Pool the generation task
        taskInformationMap.put(progressUUID, new TaskInformation(data, bean));
        generatedTimetables.put(progressUUID, threadPool.submit(() -> generateTimetable(progressUUID, initialTemperature, minTemperature, coolingRate, k)));
    }

    public void startReoptimizingSolution(Timetable timetable, UUID progressUUID, double initialTemperature, double minTemperature, double coolingRate, int k) {
        InMemoryRepository data = model.getDataRepository(timetable.getProgramName());

        // Should never happen
        if(data == null) {
            throw new RuntimeException("The program name provided has no data");
        }

        // Pool the reoptimization task
        taskInformationMap.put(progressUUID, new TaskInformation(timetable, data, bean));
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

        // Increment the number of generation tasks
        // (the synchronization with the db can only be done safely if there are no generation tasks)
        int numGen = numGenerationTasks.incrementAndGet();
        logger.info("Number of generations in progress (after starting a new generation) = {}", numGen);

        Timetable solution;
        try {
            solution = taskInformation.startGeneration(initialTemperature, minTemperature, coolingRate, k);
        } catch (Exception e) {
            numGen = numGenerationTasks.decrementAndGet();
            logger.error(e.getMessage());
            logger.info("Number of generations in progress (after a generation ended in an error) = {}", numGen);
            return null;
        }

        // The generation task was canceled (clear leftover data)
        if(solution == null) {
            synchronizationMap.remove(progressUUID);
            generatedTimetables.remove(progressUUID);
            taskInformationMap.remove(progressUUID);
            numGen = numGenerationTasks.decrementAndGet();
            logger.info("Number of generations in progress (after a generation was canceled) = {}", numGen);
            return null;
        }

        // Only locks if the final solution was generated
        try {
            countDownLatch.await();
        } catch (InterruptedException ignored) {}

        // Clear any leftover data
        generatedTimetables.remove(progressUUID);
        taskInformationMap.remove(progressUUID);

        // Decrement the number of generation tasks
        numGen = numGenerationTasks.decrementAndGet();
        logger.info("Number of generations in progress (after a generation finished) = {}", numGen);

        return solution;
    }

    public void cancelTimetableGeneration(UUID progressUUID) {
        TaskInformation taskInformation = taskInformationMap.get(progressUUID);

        if(taskInformation != null) {
            taskInformation.cancelGeneration();
        }
    }

    public void cleanup() {
        threadPool.shutdown();
        synchronizationExecutorService.shutdown();
    }

    private static class TaskInformation {
        private final ThreadMXBean bean;
        private final InMemoryRepository data;
        private final InitialSolutionGenerator<DefaultISGSolution> initialSolutionGenerator;
        private HeuristicAlgorithm<Timetable> heuristicAlgorithm;
        private final DefaultISGSolution solutionToReoptimize;

        public TaskInformation(Timetable timetable, InMemoryRepository data, ThreadMXBean bean) {
            this.data = data;
            this.bean = bean;
            initialSolutionGenerator = null;

            solutionToReoptimize = new DefaultISGSolution(data);
            for(ScheduledLesson lesson : timetable.getScheduledLessonList()) {
                ClassUnit cls = data.getClassUnit(lesson.getClassId());
                DefaultISGVariable variable = new DefaultISGVariable(cls);
                DefaultISGValue value = new DefaultISGValue(variable, lesson);

                variable.setSolution(solutionToReoptimize);
                solutionToReoptimize.addUnassignedVariable(variable);
                variable.assign(value);
            }
        }

        public TaskInformation(InMemoryRepository data, ThreadMXBean bean) {
            this.data = data;
            this.bean = bean;
            this.solutionToReoptimize = null;
            initialSolutionGenerator = new MullerBasedSolutionGenerator(data);
        }

        public double getGenerationProgress() {
            int numTasks;
            double progress = 0;

            if(initialSolutionGenerator != null) {
                progress = initialSolutionGenerator.getProgress();
                numTasks = 2;
            } else {
                // Only the reoptimization task
                numTasks = 1;
            }

            if(heuristicAlgorithm != null) {
                progress += heuristicAlgorithm.getProgress();
            }

            // The progress must be divided by the number of tasks as they have the same weight
            progress = progress / numTasks;

            // Fix the progress value (the usage of double can introduce error)
            // the progress is multiplied by 1000 for the final result to be a percentage with
            // one decimal place
            progress = Math.min(1, Math.floor(progress * 1000) / 1000.0);

            return progress;
        }

        private long getTimeElapsed(long currentTime, long startTime) {
            // Assuming the input is in nanoseconds the result is in seconds
            return (currentTime - startTime)/1_000_000_000;
        }

        public Timetable startGeneration(double initialTemperature, double minTemperature, double coolingRate, int k) throws InterruptedException, InvalidConfigurationException {
            long id = Thread.currentThread().getId();
            long startCpu = bean.getThreadCpuTime(id); // Start time in nanoseconds

            DefaultISGSolution solution;
            if(solutionToReoptimize == null) {
                solution = initialSolutionGenerator.generate();

                // Generation was canceled
                if (solution == null) {
                    return null;
                }

                logger.info("Thread {} finished generating the initial solution and it took {} seconds", id, getTimeElapsed(bean.getThreadCpuTime(id), startCpu));
            } else {
                solution = solutionToReoptimize;
            }

            long startOpt = bean.getThreadCpuTime(id); // Start time in nanoseconds
            heuristicAlgorithm = new SimulatedAnnealing(solution, initialTemperature, minTemperature, coolingRate, k);
            Timetable finalSolution = heuristicAlgorithm.execute();

            // Optimization was canceled
            if(finalSolution == null) {
                return null;
            }

            logger.info("Thread {} finished optimizing the solution and it took {} seconds", id, getTimeElapsed(bean.getThreadCpuTime(id), startOpt));

            finalSolution.setRuntime(getTimeElapsed(bean.getThreadCpuTime(id), startCpu)); // Runtime in seconds
            data.addTimetable(finalSolution);

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
