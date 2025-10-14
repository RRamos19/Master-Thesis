package thesis.view.managers;

import javafx.concurrent.Task;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import thesis.controller.ControllerInterface;
import thesis.view.ViewInterface;
import thesis.view.managers.components.ProgressBarUnit;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProgressBarManager {
    private static final int UPDATE_WAIT = 1000;
    private final ViewInterface view;
    private final ControllerInterface controller;
    private final ListView<HBox> progressContainer;
    private final Map<UUID, ProgressBarUnit> progressBarMap = new HashMap<>(); // UUID : ProgressBar

    public ProgressBarManager(ListView<HBox> parent, ControllerInterface controller, ViewInterface view) {
        this.view = view;
        this.controller = controller;
        this.progressContainer = parent;
    }

    public UUID insertProgressBar(String programName) {
        ProgressBarUnit bar = new ProgressBarUnit(programName);

        final UUID uuid = UUID.randomUUID();

        progressBarMap.put(uuid, bar);
        progressContainer.getItems().add(bar.getProgressParent());

        bar.setCancelAction((event) -> {
            controller.cancelGeneration(uuid);
            stopProgressBar(uuid);
        });

        return uuid;
    }

    public void startProgressBar(UUID progressUUID) {
        ProgressBarUnit unit = progressBarMap.get(progressUUID);

        if(unit == null) {
            throw new IllegalStateException("The UUID provided has no progress bar!");
        }

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                double maxProgress = 0;
                double progress;
                do {
                    if(isCancelled()) break;

                    progress = controller.getGenerationProgress(progressUUID);

                    if(maxProgress < progress) {
                        maxProgress = progress;
                    }

                    updateProgress(maxProgress, 1);

                    Thread.sleep(UPDATE_WAIT);
                } while(progress < 1.0);

                return null;
            }
        };

        task.setOnFailed(e -> {
            view.showExceptionMessage(e.getSource().getException());
            stopProgressBar(progressUUID);
            controller.cancelGeneration(progressUUID);
        });

        task.setOnSucceeded(e -> {
            view.showInformationAlert("The solution for the program " + unit.getProgramName() + " has been created!\nPerform a double click on it to visualize!");
            stopProgressBar(progressUUID);
            view.updateTableView();
        });

        unit.startProgressBar(task);
    }

    public void stopProgressBars() {
        for(ProgressBarUnit unit : progressBarMap.values()) {
            unit.stopProgressBar();
        }
        progressContainer.getItems().clear();
        progressBarMap.clear();
    }

    public void stopProgressBar(UUID programUUID) {
        ProgressBarUnit unit = progressBarMap.get(programUUID);

        if(unit == null) {
            throw new IllegalStateException("The UUID provided has no progress bar!");
        }

        unit.stopProgressBar();
        progressContainer.getItems().remove(unit.getProgressParent());
        progressBarMap.remove(programUUID);
    }

    public String getProgramName(UUID programUUID) {
        ProgressBarUnit unit = progressBarMap.get(programUUID);
        return unit != null ? unit.getProgramName() : null;
    }
}
