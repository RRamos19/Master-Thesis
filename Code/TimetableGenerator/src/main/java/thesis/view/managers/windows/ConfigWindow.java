package thesis.view.managers.windows;

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
import thesis.view.utils.AppIcons;
import thesis.view.managers.components.GeneralConfiguration;

public class ConfigWindow {
    private final Stage configStage;
    private final GeneralConfiguration generalConfiguration;

    // Timetable Section
    private TextField maxHourField;
    private TextField minHourField;

    // Initial Algorithm Section
    private TextField maxIterField;

    // Optimization Section
    private TextField initTempField;
    private TextField minTempField;
    private TextField coolingRateField;
    private TextField kField;

    public ConfigWindow(Window primaryWindow, GeneralConfiguration generalConfiguration) {
        this.generalConfiguration = generalConfiguration;

        configStage = new Stage();
        configStage.getIcons().addAll(AppIcons.getAppIcons());
        configStage.initStyle(StageStyle.DECORATED);
        configStage.initOwner(primaryWindow);
        configStage.initModality(Modality.WINDOW_MODAL);
        configStage.setTitle("Configuration");

        Node timetableBox = createTimetableSection();
        Node initialSolutionBox = createInitialAlgorithmSection();
        Node optimizAlgorithmBox = createOptimizationAlgorithmSection();

        VBox configurationStructure = new VBox(10, timetableBox, initialSolutionBox, optimizAlgorithmBox);

        // Save button
        Button saveBtn = new Button("Save");
        saveBtn.setOnAction(event -> {
            // Timetable Section
            generalConfiguration.setMaxHour(Integer.parseInt(maxHourField.getText()));
            generalConfiguration.setMinHour(Integer.parseInt(minHourField.getText()));

            // Initial Algorithm Section
            String maxIterString = maxIterField.getText();
            Integer maxIter = maxIterString.isEmpty() ? null : Integer.parseInt(maxIterString);
            generalConfiguration.setInitialSolutionMaxIterations(maxIter);

            // Optimization Section
            generalConfiguration.setInitialTemperature(Double.parseDouble(initTempField.getText()));
            generalConfiguration.setMinTemperature(Double.parseDouble(minTempField.getText()));
            generalConfiguration.setCoolingRate(Double.parseDouble(coolingRateField.getText()));
            generalConfiguration.setK(Integer.parseInt(kField.getText()));

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
        Label timetableLabel = new Label("Timetable Hour Limits (Only affects the GUI)");

        Label maxHourLabel = new Label("Maximum Hour:");
        maxHourField = new TextField();
        HBox maxHourBox = new HBox(5, maxHourLabel, maxHourField);
        maxHourBox.setAlignment(Pos.CENTER);

        Label minHourLabel = new Label("Minimum Hour:");
        minHourField = new TextField();
        HBox minHourBox = new HBox(5, minHourLabel, minHourField);
        minHourBox.setAlignment(Pos.CENTER);

        HBox timetableRow = new HBox(10, maxHourBox, minHourBox);
        timetableRow.setAlignment(Pos.CENTER);

        return new VBox(10, timetableLabel, timetableRow);
    }

    private Node createInitialAlgorithmSection() {
        Label initialAlgorithmLabel = new Label("Initial Solution Algorithm Configuration");

        Label maxIterLabel = new Label("Max Iterations:");
        maxIterField = new TextField();
        HBox maxIterBox = new HBox(10, maxIterLabel, maxIterField);
        maxIterBox.setAlignment(Pos.CENTER);

        return new VBox(10, initialAlgorithmLabel, maxIterBox);
    }

    private Node createOptimizationAlgorithmSection() {
        Label optimizAlgorithmLabel = new Label("Optimization Algorithm Configuration");

        Label initTempLabel = new Label("Initial Temperature:");
        initTempField = new TextField();
        HBox initTempBox = new HBox(5, initTempLabel, initTempField);
        initTempBox.setAlignment(Pos.CENTER);

        Label minTempLabel = new Label("Minimum Temperature:");
        minTempField = new TextField();
        HBox minTempBox = new HBox(5, minTempLabel, minTempField);
        minTempBox.setAlignment(Pos.CENTER);

        Label coolingRateLabel = new Label("Cooling Rate:");
        coolingRateField = new TextField();
        HBox coolingRateBox = new HBox(5, coolingRateLabel, coolingRateField);
        coolingRateBox.setAlignment(Pos.CENTER);

        Label KLabel = new Label("K:");
        kField = new TextField();
        HBox kBox = new HBox(5, KLabel, kField);
        kBox.setAlignment(Pos.CENTER);

        HBox firstOptimizAlgorithmRow = new HBox(10, initTempBox, minTempBox);
        firstOptimizAlgorithmRow.setAlignment(Pos.CENTER);

        HBox secondOptimizAlgorithmRow = new HBox(10, coolingRateBox, kBox);
        secondOptimizAlgorithmRow.setAlignment(Pos.CENTER);

        return new VBox(10, optimizAlgorithmLabel, firstOptimizAlgorithmRow, secondOptimizAlgorithmRow);
    }

    public void resetLabels() {
        // Timetable Section
        maxHourField.setText(String.valueOf(generalConfiguration.getMaxHour()));
        minHourField.setText(String.valueOf(generalConfiguration.getMinHour()));

        // Initial Algorithm Section
        Integer maxIter = generalConfiguration.getInitialSolutionMaxIterations();
        String maxIterString = maxIter != null ? String.valueOf(maxIter) : "";
        maxIterField.setText(maxIterString);

        // Optimization Section
        initTempField.setText(String.valueOf(generalConfiguration.getInitialTemperature()));
        minTempField.setText(String.valueOf(generalConfiguration.getMinTemperature()));
        coolingRateField.setText(String.valueOf(generalConfiguration.getCoolingRate()));
        kField.setText(String.valueOf(generalConfiguration.getK()));
    }

    public Stage getConfigStage() {
        return configStage;
    }
}
