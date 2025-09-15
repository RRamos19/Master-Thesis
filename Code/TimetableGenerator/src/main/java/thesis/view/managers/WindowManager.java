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
import thesis.view.utils.Defaults;

import java.util.List;

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

            VBox box = new VBox(5);
            box.setPadding(new Insets(10));
            box.setAlignment(Pos.CENTER_LEFT);

            List<String> labels = List.of(
                    "System Usage Instructions",

                    "1. For the system to function correctly, follow these steps:",
                    "\tConnect to the database",
                    "\tEnter the database IP address and Port in the corresponding fields.",
                    "\tEnter the username and password in the corresponding fields.",
                    "\tPress the \"Connect\" button.",
                    "\tNote - Alternatively, you may import one or more files into the system instead of connecting to a database.",

                    "2. Verify data availability",
                    "\tOnce data is loaded, a program name will appear in the top-left corner of the interface.",
                    "\tBelow it, you will find selectable items. When clicked, these display the contents of the corresponding tables in the central area of the window.",

                    "3. Generate timetables",
                    "\tPress the button located at the bottom-right corner to start the generation process.",
                    "\tA progress bar at the bottom of the window will indicate task progress.",
                    "\tAfter completion, a new item called \"Timetable\" will appear on the left. Selecting it displays all solutions for the chosen program.",

                    "4. Task management",
                    "\tThe application supports multiple generation tasks running in parallel.",
                    "\tMultiple tasks can be executed at a time. Each one has a button to cancel said task.",

                    "5. Exporting the data",
                    "\tOn the top menu, the option \"File\" contains all the choices of exportation.",
                    "\tUpon choosing one of the options, all the data of the chosen program will be exported in the choosen format."
            );

            for(String line : labels) {
                Label textLabel = new Label(line);
                textLabel.setTextAlignment(TextAlignment.LEFT);

                box.getChildren().add(textLabel);
            }

            // Close button
            Button closeBtn = new Button("Close");
            closeBtn.setOnAction(event -> tutorialWindow.close());
            HBox closeBTNBox = new HBox(closeBtn);
            closeBTNBox.setAlignment(Pos.CENTER_RIGHT);

            box.getChildren().add(closeBTNBox);

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
