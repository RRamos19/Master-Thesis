package thesis.controller.managers.windows;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import thesis.controller.ControllerInterface;
import thesis.view.utils.AppIcons;
import thesis.controller.managers.components.GeneralConfiguration;

public class ConfigWindow {
    private final Stage configStage;
    private final GeneralConfiguration generalConfiguration;

    // Timetable Section
    private TextField maxHourField;
    private TextField minHourField;

    // Optimization Section
    private TextField initTempField;
    private TextField minTempField;
    private TextField coolingRateField;
    private TextField kField;

    // Database Section
    private TextField syncTimeField;

    public ConfigWindow(Window primaryWindow, GeneralConfiguration generalConfiguration, ControllerInterface controller) {
        this.generalConfiguration = generalConfiguration;

        configStage = new Stage();
        configStage.getIcons().addAll(AppIcons.getAppIcons());
        configStage.initStyle(StageStyle.DECORATED);
        configStage.initOwner(primaryWindow);
        configStage.initModality(Modality.WINDOW_MODAL);
        configStage.setTitle("Configuration");

        Node timetableBox = createTimetableSection();
        Node optimizAlgorithmBox = createOptimizationAlgorithmSection();
        Node databaseBox = createDatabaseSection();

        VBox configurationStructure = new VBox(10, timetableBox, optimizAlgorithmBox, databaseBox);

        // Save button
        Button saveBtn = new Button("Save");
        saveBtn.setOnAction(event -> {
            try {
                // Timetable Section
                int maxHour = Integer.parseInt(maxHourField.getText());
                int minHour = Integer.parseInt(minHourField.getText());

                if(maxHour < 0 || maxHour > 24) throw new RuntimeException("The Max Hour specified should be between 0 and 24 hours");
                if(minHour < 0 || minHour > 24 || minHour > maxHour) throw new RuntimeException("The Min Hour specified should be between 0 and 24 hours and lower than the Max Hour");

                generalConfiguration.setMaxHour(maxHour);
                generalConfiguration.setMinHour(minHour);

                // Optimization Section
                generalConfiguration.setInitialTemperature(Double.parseDouble(initTempField.getText()));
                generalConfiguration.setMinTemperature(Double.parseDouble(minTempField.getText()));
                generalConfiguration.setCoolingRate(Double.parseDouble(coolingRateField.getText()));
                generalConfiguration.setK(Integer.parseInt(kField.getText()));

                // Database Section
                generalConfiguration.setDatabaseSynchronizationTimeMinutes(Integer.parseInt(syncTimeField.getText()));
            } catch (Exception e) {
                controller.showExceptionMessage(e);
                return;
            }

            configStage.close();
        });
        HBox saveBTNbox = new HBox(saveBtn);
        saveBTNbox.setAlignment(Pos.CENTER_RIGHT);

        VBox box = new VBox(10, configurationStructure, saveBTNbox);
        box.setPadding(new Insets(10));
        box.setAlignment(Pos.CENTER_LEFT);

        configStage.setScene(new Scene(box));
    }

    private Node createTimetableSection() {
        Label timetableLabel = new Label("Timetable Hour Limits (Affects the GUI and graphical exportations)");
        timetableLabel.setStyle("-fx-font-weight: bold");

        // Change the size for at most 2 characters (value found by trial and error)
        int hourFieldWidth = 30;

        Label maxHourLabel = new Label("Maximum Hour:");
        maxHourField = new TextField();
        maxHourField.setPrefWidth(hourFieldWidth);
        HBox maxHourBox = new HBox(5, maxHourLabel, maxHourField);
        maxHourBox.setAlignment(Pos.CENTER);

        Label minHourLabel = new Label("Minimum Hour:");
        minHourField = new TextField();
        minHourField.setPrefWidth(hourFieldWidth);
        HBox minHourBox = new HBox(5, minHourLabel, minHourField);
        minHourBox.setAlignment(Pos.CENTER);

        HBox timetableRow = new HBox(10, maxHourBox, minHourBox);
        timetableRow.setAlignment(Pos.CENTER);

        return new VBox(10, timetableLabel, timetableRow);
    }

    private Node createOptimizationAlgorithmSection() {
        Label optimizAlgorithmLabel = new Label("Optimization Algorithm Configuration");
        optimizAlgorithmLabel.setStyle("-fx-font-weight: bold");

        int tempFieldWidth = 80;

        Label initTempLabel = new Label("Initial Temperature:");
        initTempField = new TextField();
        initTempField.setPrefWidth(tempFieldWidth);
        HBox initTempBox = new HBox(5, initTempLabel, initTempField);
        initTempBox.setAlignment(Pos.CENTER);

        Label minTempLabel = new Label("Minimum Temperature:");
        minTempField = new TextField();
        minTempField.setPrefWidth(tempFieldWidth);
        HBox minTempBox = new HBox(5, minTempLabel, minTempField);
        minTempBox.setAlignment(Pos.CENTER);

        Label coolingRateLabel = new Label("Cooling Rate:");
        coolingRateField = new TextField();
        coolingRateField.setPrefWidth(100);
        HBox coolingRateBox = new HBox(5, coolingRateLabel, coolingRateField);
        coolingRateBox.setAlignment(Pos.CENTER);

        Label KLabel = new Label("K:");
        kField = new TextField();
        kField.setPrefWidth(50);
        HBox kBox = new HBox(5, KLabel, kField);
        kBox.setAlignment(Pos.CENTER);

        HBox firstOptimizAlgorithmRow = new HBox(10, initTempBox, minTempBox);
        firstOptimizAlgorithmRow.setAlignment(Pos.CENTER);

        HBox secondOptimizAlgorithmRow = new HBox(10, coolingRateBox, kBox);
        secondOptimizAlgorithmRow.setAlignment(Pos.CENTER);

        return new VBox(10, optimizAlgorithmLabel, firstOptimizAlgorithmRow, secondOptimizAlgorithmRow);
    }

    private Node createDatabaseSection() {
        Label databaseLabel = new Label("Database Configuration");
        databaseLabel.setStyle("-fx-font-weight: bold");

        Label syncTimeLabel = new Label("Time Between Synchronizations (in minutes):");
        syncTimeField = new TextField();
        syncTimeField.setPrefWidth(50);
        HBox syncTimeBox = new HBox(5, syncTimeLabel, syncTimeField);
        syncTimeBox.setAlignment(Pos.CENTER);

        return new VBox(10, databaseLabel, syncTimeBox);
    }

    public void resetLabels() {
        // Timetable Section
        maxHourField.setText(String.valueOf(generalConfiguration.getMaxHour()));
        minHourField.setText(String.valueOf(generalConfiguration.getMinHour()));

        // Optimization Section
        initTempField.setText(String.valueOf(generalConfiguration.getInitialTemperature()));
        minTempField.setText(String.valueOf(generalConfiguration.getMinTemperature()));
        coolingRateField.setText(String.valueOf(generalConfiguration.getCoolingRate()));
        kField.setText(String.valueOf(generalConfiguration.getK()));

        // Database Section
        syncTimeField.setText(String.valueOf(generalConfiguration.getDatabaseSynchronizationTimeMinutes()));
    }

    public Stage getConfigStage() {
        return configStage;
    }
}
