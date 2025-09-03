package thesis.view.managers;

import javafx.animation.KeyFrame;
import javafx.scene.layout.Pane;
import thesis.view.managers.components.ProgressBarUnit;

import java.util.HashMap;
import java.util.Map;

public class ProgressBarManager {
    private final Pane progressContainer;
    private final Map<String, ProgressBarUnit> progressBarMap = new HashMap<>(); // ProgramName : ProgressBar

    public ProgressBarManager(Pane parent) {
        this.progressContainer = parent;
    }

    public void insertProgressBar(String programName) {
        ProgressBarUnit bar = new ProgressBarUnit(programName);

        progressBarMap.put(programName, bar);

        progressContainer.getChildren().add(bar.getProgressParent());
    }

    public void startTimeline(String programName, int cycleCount, KeyFrame ... keyframes) {
        ProgressBarUnit unit = progressBarMap.get(programName);

        if(unit == null) {
            throw new IllegalStateException("Program has no progress bar!");
        }

        unit.startTimeline(cycleCount, keyframes);
    }

    public void stopAndClearTimelines() {
        for(ProgressBarUnit unit : progressBarMap.values()) {
            unit.stopAndClearTimeline();
        }
    }

    public void stopAndClearTimeline(String programName) {
        ProgressBarUnit unit = progressBarMap.get(programName);

        if(unit == null) {
            throw new IllegalStateException("Program has no progress bar!");
        }

        unit.stopAndClearTimeline();
        progressContainer.getChildren().remove(unit.getProgressParent());
        progressBarMap.remove(programName);
    }

    public void setProgress(String programName, double progress) {
        ProgressBarUnit unit = progressBarMap.get(programName);

        if(unit == null) {
            throw new IllegalStateException("Program has no progress bar!");
        }

        // A verification is made to avoid a progress bar the goes back and forth (which may happen
        // because the progress of the initial solution is based on the unscheduled classes which can
        // increase or decrease depending on the situation)
        if(unit.getProgress() < progress) {
            unit.setProgress(progress);
        }
    }

    public boolean exists(String programName) {
        return progressBarMap.get(programName) != null;
    }
}
