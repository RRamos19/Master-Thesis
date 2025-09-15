package thesis.view.managers.components;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;

public class ProgressBarUnit {
    private final ProgressBar progressBar;
    private final HBox progressParent;
    private Timeline timeline;

    public ProgressBarUnit(String barName) {
        Label progressBarName = new Label(barName + " :");

        Label progressBarProgress = new Label();
        progressBarProgress.setMaxWidth(Double.MAX_VALUE);
        progressBarProgress.setAlignment(Pos.CENTER);
        progressBar = new ProgressBar(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);

        progressBarProgress.textProperty().bind(
            progressBar.progressProperty().multiply(100).asString("%.1f%%")
        );

        StackPane progressUnit = new StackPane(progressBar, progressBarProgress);

        this.progressParent = new HBox(5, progressBarName, progressUnit);
        HBox.setHgrow(progressUnit, Priority.ALWAYS);
    }

    public void setProgress(Double progress) {
        progressBar.setProgress(progress);
    }

    public double getProgress() {
        return progressBar.getProgress();
    }

    public Node getProgressParent() {
        return progressParent;
    }

    public void startTimeline(int cycleCount, KeyFrame... keyFrames) {
        if(timeline != null) {
            throw new IllegalStateException("There is a timeline already created for this program!");
        }

        timeline = new Timeline(keyFrames);
        timeline.setCycleCount(cycleCount);
        timeline.play();
    }

    public void stopAndClearTimeline() {
        timeline.stop();
        timeline = null;
    }
}
