package thesis.view.managers;

import javafx.animation.KeyFrame;
import javafx.event.EventHandler;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import thesis.view.managers.components.ProgressBarUnit;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProgressBarManager {
    private final ListView<HBox> progressContainer;
    private final Map<UUID, ProgressBarUnit> progressBarMap = new HashMap<>(); // UUID : ProgressBar

    public ProgressBarManager(ListView<HBox> parent) {
        this.progressContainer = parent;
    }

    public UUID insertProgressBar(String programName) {
        ProgressBarUnit bar = new ProgressBarUnit(programName);

        UUID uuid;
        do {
             uuid = UUID.randomUUID();
        } while(progressBarMap.get(uuid) != null);

        progressBarMap.put(uuid, bar);
        progressContainer.getItems().add(bar.getProgressParent());

        return uuid;
    }

    public void setCancelAction(UUID progressUUID, EventHandler<MouseEvent> cancelAction) {
        ProgressBarUnit unit = progressBarMap.get(progressUUID);

        if(unit == null) {
            throw new IllegalStateException("The UUID provided has no progress bar!");
        }

        unit.setCancelAction(cancelAction);
    }

    public void startTimeline(UUID progressUUID, int cycleCount, KeyFrame ... keyframes) {
        ProgressBarUnit unit = progressBarMap.get(progressUUID);

        if(unit == null) {
            throw new IllegalStateException("The UUID provided has no progress bar!");
        }

        unit.startTimeline(cycleCount, keyframes);
    }

    public void stopAndClearTimelines() {
        for(ProgressBarUnit unit : progressBarMap.values()) {
            unit.stopAndClearTimeline();
        }
    }

    public void stopAndClearTimeline(UUID programUUID) {
        ProgressBarUnit unit = progressBarMap.get(programUUID);

        if(unit == null) {
            throw new IllegalStateException("The UUID provided has no progress bar!");
        }

        unit.stopAndClearTimeline();
        progressContainer.getItems().remove(unit.getProgressParent());
        progressBarMap.remove(programUUID);
    }

    public void setProgress(UUID programUUID, double progress) {
        ProgressBarUnit unit = progressBarMap.get(programUUID);

        if(unit == null) {
            throw new IllegalStateException("The UUID provided has no progress bar!");
        }

        // A verification is made to avoid a progress bar the goes back and forth (which may happen
        // because the progress of the initial solution is based on the unscheduled classes which can
        // increase or decrease depending on the situation)
        if(unit.getProgress() < progress) {
            unit.setProgress(progress);
        }
    }

    public String getProgramName(UUID programUUID) {
        ProgressBarUnit unit = progressBarMap.get(programUUID);
        return unit != null ? unit.getProgramName() : null;
    }
}
