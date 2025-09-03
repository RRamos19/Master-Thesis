package thesis.view.managers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import thesis.view.utils.AppIcons;
import thesis.view.managers.components.GeneralConfiguration;
import thesis.view.managers.windows.ConfigWindow;
import thesis.view.managers.windows.ExceptionMessageWindow;

public class WindowManager {
    private final Window primaryWindow;

    private ExceptionMessageWindow exceptionMessageWindow;
    private Stage tutorialWindow;
    private ConfigWindow configWindow;

    public WindowManager(Window primaryWindow) {
        this.primaryWindow = primaryWindow;
    }

    public Stage getExceptionMessage(Exception e) {
        if(exceptionMessageWindow == null) {
            exceptionMessageWindow = new ExceptionMessageWindow(primaryWindow);
        }

        exceptionMessageWindow.setMessages(e);

        return exceptionMessageWindow.getExceptionStage();
    }

    public Stage getTutorialWindow() {
        if(tutorialWindow == null) {
            tutorialWindow = new Stage();
            tutorialWindow.getIcons().addAll(AppIcons.getAppIcons());
            tutorialWindow.initStyle(StageStyle.DECORATED);
            tutorialWindow.initOwner(primaryWindow);
            tutorialWindow.initModality(Modality.WINDOW_MODAL);
            tutorialWindow.setTitle("Tutorial");

            // Message to be displayed
            Label textLabel = new Label("For the correct functioning of the system start by either connecting to the database, by writting the IP and Port of the database on the respective fields, followed by pressing the button \"Connect\", or importing a file (or multiple files) into the system\n" +
                    "Now that the system has data there should be an item selected on the top left which displays the program name. Below are some items which when pressed display what is currently stored on the table in the middle of the window.\n" +
                    "There is a button on the bottom right which starts the generation process. Upon clicking the button there should be a progress bar on the bottom that displays the progress of the task. Upon completion there should be a new item on the left called \"Timetable\" which displays all the solutions for the selected program\n" +
                    "The application supports multiple generation tasks at a time but only one task per program at a time.");
            textLabel.setWrapText(true);
            textLabel.setTextAlignment(TextAlignment.JUSTIFY);

            // Close button
            Button closeBtn = new Button("Close");
            closeBtn.setOnAction(event -> tutorialWindow.close());
            HBox closeBTNbox = new HBox(closeBtn);
            closeBTNbox.setAlignment(Pos.CENTER_RIGHT);

            VBox box = new VBox(5, textLabel, closeBTNbox);
            box.setPadding(new Insets(10));
            box.setAlignment(Pos.CENTER_LEFT);

            VBox.setVgrow(textLabel, Priority.ALWAYS);

            tutorialWindow.setScene(new Scene(box));
            tutorialWindow.setAlwaysOnTop(true);
        }

        return tutorialWindow;
    }

    public Stage getConfigWindow(GeneralConfiguration generalConfiguration) {
        if(configWindow == null) {
            configWindow = new ConfigWindow(primaryWindow, generalConfiguration);
        }

        configWindow.resetLabels();
        return configWindow.getConfigStage();
    }
}
