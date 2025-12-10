package thesis;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import thesis.controller.Controller;
import thesis.controller.ControllerInterface;
import thesis.model.Model;
import thesis.model.ModelInterface;
import thesis.model.exporter.DataExporter;
import thesis.model.exporter.TimetableDataExporter;
import thesis.model.parser.InputFormatParser;
import thesis.model.parser.InputFileReader;
import thesis.view.JavaFXView;
import thesis.view.ViewInterface;
import thesis.view.utils.AppIcons;

public class LauncherFX extends Application {
    ControllerInterface controller;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Create the importer and exporter
        DataExporter dataExporter = new TimetableDataExporter();
        InputFileReader inputReader = new InputFormatParser();

        // Create the Model, View and Controller
        controller = new Controller();
        ModelInterface model = new Model(inputReader, dataExporter);
        ViewInterface view = new JavaFXView(controller);

        // Set all the needed relations
        controller.setModel(model);
        controller.setView(view);
        controller.setPrimaryWindow(primaryStage);

        // Initialize the controller
        controller.initialize();

        // Show the application window
        primaryStage.getIcons().addAll(AppIcons.getAppIcons());
        primaryStage.setTitle("Timetable Generator");
        primaryStage.setScene(new Scene(view.getRoot()));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void stop() {
        if (controller != null) {
            controller.cleanup();
        }
    }
}
