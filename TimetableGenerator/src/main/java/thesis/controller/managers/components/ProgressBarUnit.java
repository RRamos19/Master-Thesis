package thesis.controller.managers.components;

import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;

public class ProgressBarUnit {
    private final String programName;
    private final ProgressBar progressBar;
    private final HBox progressParent;
    private final Button cancelButton;
    private Task<Void> task;

    public ProgressBarUnit(String programName) {
        this.programName = programName;
        Label progressBarName = new Label(programName + " :");

        Label progressBarProgress = new Label();
        progressBarProgress.setMaxWidth(Double.MAX_VALUE);
        progressBarProgress.setAlignment(Pos.CENTER);
        progressBar = new ProgressBar(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);

        cancelButton = new Button("X");

        progressBarProgress.textProperty().bind(
            progressBar.progressProperty().multiply(100).asString("%.1f%%")
        );

        StackPane progressUnit = new StackPane(progressBar, progressBarProgress);

        this.progressParent = new HBox(5, progressBarName, progressUnit, cancelButton);
        HBox.setHgrow(progressUnit, Priority.ALWAYS);
        progressParent.setAlignment(Pos.CENTER);
    }

    public void setCancelAction(EventHandler<MouseEvent> cancelAction) {
        cancelButton.addEventHandler(MouseEvent.MOUSE_RELEASED, cancelAction);
    }

    public HBox getProgressParent() {
        return progressParent;
    }

    public String getProgramName() {
        return programName;
    }

    public void startProgressBar(Task<Void> task) {
        this.task = task;
        progressBar.progressProperty().bind(task.progressProperty());

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    public void stopProgressBar() {
        task.cancel();
    }
}
