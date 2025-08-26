package thesis.view;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import thesis.controller.ControllerInterface;
import thesis.model.domain.InMemoryRepository;
import thesis.model.domain.elements.ScheduledLesson;
import thesis.model.domain.elements.Teacher;
import thesis.model.domain.elements.Timetable;
import thesis.model.domain.elements.TableDisplayable;
import thesis.utils.DoubleToolkit;

import java.io.File;
import java.util.*;

public class JavaFXController implements ViewInterface {
    private static final List<String> DAYS_OF_WEEK = List.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");
    private static final int PROGRESSBAR_UPDATE_SECONDS = 2;
    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 600;

    private final Map<String, ProgressBar> progressBarMap = new HashMap<>();      // ProgramName : ProgressBar
    private final Map<String, Timeline> activeTimelines = new HashMap<>();        // ProgramName : Timeline

    private ControllerInterface controller;
    private boolean isConnected = false;
    private volatile String chosenProgram;
    private Window primaryWindow;
    private generalConfiguration generalConfiguration;

    @FXML
    private TextField ipField;

    @FXML
    private TreeView<String> treeView;

    @FXML
    private TextField portField;

    @FXML
    private Button connectBtn;

    @FXML
    private VBox progressContainer;

    @FXML
    private ChoiceBox<String> programsChoiceBox;

    @FXML
    private TableView<TableDisplayable> tableView;

    @Override
    public void setController(ControllerInterface controller) {
        this.controller = controller;
    }

    @FXML
    private void connectEvent() {
        if (!isConnected) {
            if(ipField.getText().isEmpty()) {
                showErrorMessage("The ip field should not be empty");
                return;
            }
            if(portField.getText().isEmpty()) {
                showErrorMessage("The port field should not be empty");
                return;
            }

            connectToDB();
            isConnected = true;
        } else {
            disconnectFromDB();
            isConnected = false;
        }

        ipField.setDisable(isConnected);
        portField.setDisable(isConnected);
        connectBtn.setText(isConnected ? "Disconnect" : "Connect");
    }

    private void connectToDB() {

    }

    private void disconnectFromDB() {

    }

    private FileChooser createXmlFileChooser() {
        FileChooser fileChooser = new FileChooser();

        // Sets the initial location of the FileChooser as the directory where the application is located
        File initialDirectory = new File(String.valueOf(ClassLoader.getSystemResource("."))).getAbsoluteFile().getParentFile();
        if(initialDirectory != null && initialDirectory.isDirectory()) {
            fileChooser.setInitialDirectory(initialDirectory);
        }

        FileChooser.ExtensionFilter xmlFileFilter = new FileChooser.ExtensionFilter("XML Files", "*.xml");
        fileChooser.getExtensionFilters().add(xmlFileFilter);
        fileChooser.setSelectedExtensionFilter(xmlFileFilter);

        return fileChooser;
    }

    @FXML
    private void importDataITCEvent() {
        FileChooser fileChooser = createXmlFileChooser();

        List<File> chosenFiles = fileChooser.showOpenMultipleDialog(new Stage());

        if(chosenFiles != null) {
            for(File file : chosenFiles) {
                controller.importITCData(file);
            }

            updateStoredPrograms();
        }
    }

    private void updateStoredPrograms() {
        String oldChosenProgram = chosenProgram;
        Set<String> storedPrograms = controller.getStoredPrograms();

        // Choose the first element if there was no program already chosen
        if(oldChosenProgram == null && !storedPrograms.isEmpty()) {
            oldChosenProgram = (String) storedPrograms.toArray()[0];
        }

        programsChoiceBox.setItems(FXCollections.observableList(new ArrayList<>(storedPrograms)));

        if(oldChosenProgram != null && storedPrograms.contains(oldChosenProgram)) {
            programsChoiceBox.setValue(oldChosenProgram);
        }
    }

    @FXML
    private void configMenuEvent() {

    }

    @FXML
    private void exportDataITCEvent() {
        controller.exportDataToITC(chosenProgram);
    }

    @FXML
    private void exportDataCSVEvent() {
        controller.exportToCSV(chosenProgram);
    }

    @FXML
    private void exportSolutionsITCEvent() {
        controller.exportSolutionsToITC(chosenProgram);
    }

    @FXML
    private void exportSolutionsPNGEvent() {
        controller.exportToPNG(chosenProgram);
    }

    @FXML
    private void exportSolutionsPDFEvent() {
        controller.exportToPDF(chosenProgram);
    }

    @FXML
    private void aboutMenuEvent() {

    }

    @FXML
    private void generateSolutionEvent() {
        if(chosenProgram == null) {
            showErrorMessage("A program must be chosen before starting the generation of a solution!");
            return;
        }
        if(activeTimelines.get(chosenProgram) != null) {
            showErrorMessage("There is a solution generation already in progress!");
            return;
        }

        Node parent = insertProgressBar(chosenProgram);

        controller.startGeneratingSolution(chosenProgram,
                generalConfiguration.getInitialSolutionMaxIterations(),
                generalConfiguration.getInitialTemperature(),
                generalConfiguration.getMinTemperature(),
                generalConfiguration.getCoolingRate(),
                generalConfiguration.getK());

        String generatingSolutionProgram = chosenProgram;

        Timeline progressBarUpdater = new Timeline(new KeyFrame(Duration.seconds(PROGRESSBAR_UPDATE_SECONDS), e -> progressBarUpdate(e, parent, generatingSolutionProgram)));
        progressBarUpdater.setCycleCount(Timeline.INDEFINITE);
        progressBarUpdater.play();
        activeTimelines.put(generatingSolutionProgram, progressBarUpdater);
    }

    private Node insertProgressBar(String programName) {
        Label taskName = new Label(programName + " :");
        ProgressBar bar = new ProgressBar(0);
        bar.setMaxWidth(Double.MAX_VALUE);
        Label progressLabel = new Label("0%");

        progressLabel.textProperty().bind(
                bar.progressProperty().multiply(100).asString("%.1f%%")
        );

        StackPane progressPane = new StackPane(bar, progressLabel);
        StackPane.setAlignment(bar, Pos.CENTER_LEFT);
        StackPane.setAlignment(progressLabel, Pos.CENTER);

        HBox taskBox = new HBox(5, taskName, progressPane);
        HBox.setHgrow(progressPane, Priority.ALWAYS);

        progressContainer.getChildren().add(taskBox);

        progressBarMap.put(programName, bar);

        return taskBox;
    }

    private void progressBarUpdate(ActionEvent ignoredEvent, Node progressBarParent, String generatingSolutionProgram) {
        double progress = 0;

        try {
            progress = controller.getGenerationProgress(generatingSolutionProgram);
        } catch (Exception e) {
            stopAndClearTimeline(activeTimelines.get(generatingSolutionProgram));
            activeTimelines.remove(generatingSolutionProgram);
        }

        ProgressBar progressBar = progressBarMap.get(generatingSolutionProgram);

        // Should never happen
        if(progressBar == null) {
            throw new RuntimeException("The activity should have a progress bar but it doesn't");
        }

        // A verification is made to avoid a progress bar the goes back and forth (which may happen
        // because the progress of the initial solution is based on the unscheduled classes which can
        // increase or decrease depending on the situation)
        if(progressBar.getProgress() < progress) {
            progressBar.setProgress(progress);
        }

        if (DoubleToolkit.isEqual(progress, 1)) {
            Timeline progressBarUpdater = activeTimelines.get(generatingSolutionProgram);

            stopAndClearTimeline(progressBarUpdater);
            activeTimelines.remove(generatingSolutionProgram);

            progressBarMap.remove(generatingSolutionProgram);
            progressContainer.getChildren().remove(progressBarParent);

            // If the chosenProgram is the same as the program of the solution, the treeView must be updated
            if(generatingSolutionProgram.equals(chosenProgram)) {
                populateTreeView(chosenProgram);
            }

            showInformationMessage("The solution for the program " + generatingSolutionProgram + " has been created!\nPerform a double click on it to visualize!");
        }
    }

    private void stopAndClearTimeline(Timeline timeline) {
        // Should never happen
        if(timeline == null) {
            throw new RuntimeException("The timeline provided doesn't exist");
        }

        timeline.stop();
        timeline.getKeyFrames().clear();
    }

    private void populateTreeView(String progname) {
        TreeItem<String> root = new TreeItem<>("Data");

        Map<String, List<TableDisplayable>> data = controller.getAllDisplayableData(progname);

        if(!data.isEmpty()) {
            data.keySet().forEach((s) -> {
                TreeItem<String> item = new TreeItem<>(s);
                root.getChildren().add(item);
            });
        }

        treeView.setRoot(root);
    }

    public void showTutorial() {

    }

    public void showInformationMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message);

        alert.show();
    }

    public void showErrorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);

        alert.show();
    }

    public void showTimetable(InMemoryRepository data, Timetable timetable) {
        Stage stage = new Stage();
        stage.setTitle(timetable.getProgramName() + " timetable");

        if(primaryWindow != null) {
            stage.initOwner(primaryWindow);
            stage.initModality(Modality.WINDOW_MODAL);
        }

        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setPadding(new Insets(10));
        //gridPane.setGridLinesVisible(true);

        Map<String, List<TimetableNodeGridLocation>> nodesToAdd = new HashMap<>(); // day + startSlot : List<TimetableNodeGridLocation>
        int maxSlot = 0;
        int minSlot = Integer.MAX_VALUE;
        // maxDay is reduced by 1 because the count starts with 0
        int maxDay = data.getTimetableConfiguration().getNumDays() - 1;

        Map<Integer, Integer> maxColSpan = new HashMap<>();

        for(ScheduledLesson scheduledLesson : timetable.getScheduledLessonList()) {
            String days = scheduledLesson.getDaysBinaryString();
            for(int i=0; i< days.length(); i++) {
                if(days.charAt(i) == '1') {
                    int startSlot = scheduledLesson.getStartSlot();
                    int endSlot = scheduledLesson.getEndSlot();

                    if(startSlot < minSlot) {
                        minSlot = startSlot;
                    }
                    if(endSlot > maxSlot) {
                        maxSlot = endSlot;
                    }

                    List<TimetableNodeGridLocation> gridLocationArrayList = nodesToAdd.computeIfAbsent("" + i + startSlot, (s) -> new ArrayList<>());

                    StringBuilder nodeString = new StringBuilder(scheduledLesson.getClassId() + "\n[" + data.getProgramName() + "]\n");
                    boolean addedTeacher = false;

                    List<Teacher> teacherList = scheduledLesson.getTeachers();
                    if(teacherList != null) {
                        nodeString.append('[');
                        for (Teacher teacher : scheduledLesson.getTeachers()) {
                            if (addedTeacher) {
                                nodeString.append("; ");
                            }
                            nodeString.append('(').append(teacher.getName()).append(')');

                            addedTeacher = true;
                        }
                        nodeString.append("]\n");
                    }

                    nodeString.append('[').append(scheduledLesson.getRoomId()).append(']');

                    Label nodeLabel = new Label(nodeString.toString());
                    nodeLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                    nodeLabel.setPadding(new Insets(1, 5, 1, 5));
                    // Apply a border to the label
                    nodeLabel.setStyle("-fx-border-color: black; -fx-border-width: 1;");

                    // If the list is not empty then a sub column is needed to correctly represent the node
                    int colIdx = gridLocationArrayList.size();
                    gridLocationArrayList.add(new TimetableNodeGridLocation(nodeLabel, i, startSlot, scheduledLesson.getLength(), colIdx));

                    int colSpan = maxColSpan.getOrDefault(i, 1);
                    if(colIdx+1 > colSpan) {
                        maxColSpan.put(i, colIdx+1);
                    }
                }
            }
        }

        Map<Integer, Integer> dayOffsets = new HashMap<>();
        int startColIdx = 1;

        for (int day = 0; day < maxDay; day++) {
            dayOffsets.put(day, startColIdx);
            startColIdx += maxColSpan.getOrDefault(day, 1);

            // Set all the columns to the same width
            int colSpan = maxColSpan.getOrDefault(day, 1);
            for (int c = 0; c < colSpan; c++) {
                ColumnConstraints cc = new ColumnConstraints();
                cc.setPercentWidth(100.0 / colSpan);
                gridPane.getColumnConstraints().add(cc);
            }
        }

        // Add the boxes into the GridPane
        for(List<TimetableNodeGridLocation> nodeData : nodesToAdd.values()) {
            for(TimetableNodeGridLocation nodeGridLocation : nodeData) {
                int baseCol = dayOffsets.get(nodeGridLocation.getDay());
                int col = baseCol + nodeGridLocation.getSubColumn();
                // A value of 1 is added to the column index because column 0 is reserved for the time of the lessons
                // The same goes for the rows but for days of week
                StackPane nodePane = new StackPane(nodeGridLocation.getNode());
                nodePane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                nodePane.setStyle("-fx-border-color: black; -fx-border-width: 1;");

                gridPane.add(nodePane, col, nodeGridLocation.getStart() + 1, 1, nodeGridLocation.getDuration());
            }
        }

        int numSlots = data.getTimetableConfiguration().getSlotsPerDay();
        int minutesPerSlot = 24*60/numSlots;

        int minHourInSlots = generalConfiguration.getMinHour() * 60 / minutesPerSlot;
        int maxHourInSlots = generalConfiguration.getMaxHour() * 60 / minutesPerSlot;

        // These calculations allow the timetable to expand until either the first or last lessons appear
        // Example: If the min hour in the configuration was 8h but, for some reason, there was a lesson
        // scheduled at 6h. The min hour of the timetable will be 6h.
        minSlot = Math.min(minSlot, minHourInSlots);
        maxSlot = Math.max(maxSlot, maxHourInSlots);

        // Add the time slots on the first column
        int timeblock_index = 1;
        for(int i = 0; i < numSlots; i++) {
            int startMin = minutesPerSlot*i;
            int startHour = startMin / 60;

            if(i < minSlot) {
                continue;
            }

            int endMin = startMin + minutesPerSlot;
            int endHour = endMin / 60;

            if(i > maxSlot) {
                System.out.println("This should never happen!");
                break;
            }

            startMin = startMin % 60;
            endMin = endMin % 60;

            String timeString = String.format("%02d:%02d - %02d:%02d", startHour, startMin, endHour, endMin);

            Label timeLabel = new Label(timeString);

            StackPane timePane = new StackPane(timeLabel);
            timePane.setStyle("-fx-border-color: black; -fx-border-width: 1;");
            StackPane.setAlignment(timeLabel, Pos.CENTER);
            StackPane.setMargin(timeLabel, new Insets(1, 5, 1, 5));

            gridPane.add(timePane, 0, timeblock_index++);
        }

        // Add the days of week on the first row
        for(int day = 0; day < maxDay; day++) {
            int colSpan = maxColSpan.getOrDefault(day, 1);
            int baseCol = dayOffsets.get(day);

            Label dayOfWeekLabel = new Label(DAYS_OF_WEEK.get(day));

            StackPane dayOfWeekPane = new StackPane(dayOfWeekLabel);
            dayOfWeekPane.setStyle("-fx-border-color: black; -fx-border-width: 1;");
            StackPane.setAlignment(dayOfWeekLabel, Pos.CENTER);
            StackPane.setMargin(dayOfWeekPane, new Insets(1, 5, 1, 5));

            gridPane.add(dayOfWeekPane, baseCol, 0, colSpan, 1);
        }

        ScrollPane scrollPane = new ScrollPane(gridPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        stage.setScene(new Scene(scrollPane, DEFAULT_WIDTH, DEFAULT_HEIGHT));
        stage.show();
    }

    @Override
    public void showExceptionMessage(Exception e) {
        Stage errorStage = new Stage();
        errorStage.initModality(Modality.APPLICATION_MODAL);
        errorStage.setTitle("Error message");

        // Primary message to be displayed
        Label exceptionMessage = new Label(e.getMessage());
        exceptionMessage.setWrapText(true);

        // Stacktrace to give more insight of the error
        TextArea stacktraceMessage = new TextArea(Arrays.toString(e.getStackTrace()));
        stacktraceMessage.setEditable(false);
        stacktraceMessage.setWrapText(true);

        // Area where the stacktrace will be displayed (starts hidden)
        TitledPane titledPane = new TitledPane("Show stracktrace", stacktraceMessage);
        titledPane.setExpanded(false);
        titledPane.setAnimated(false);
        titledPane.expandedProperty().addListener((obs, oldV, newV) ->
                Platform.runLater(errorStage::sizeToScene)
        );

        // Close button
        Button closeBtn = new Button("Close");
        closeBtn.setOnAction(event -> errorStage.close());
        HBox hbox = new HBox(closeBtn);
        hbox.setAlignment(Pos.CENTER_RIGHT);

        VBox box = new VBox(10, exceptionMessage, titledPane, hbox);
        box.setPadding(new Insets(10));
        box.setAlignment(Pos.CENTER_LEFT);

        VBox.setVgrow(exceptionMessage, Priority.NEVER);
        VBox.setVgrow(titledPane, Priority.NEVER);
        VBox.setVgrow(hbox, Priority.NEVER);

        errorStage.setScene(new Scene(box));
        errorStage.show();
    }

    private void clearTableView() {
        tableView.getColumns().clear();
        tableView.getItems().clear();
    }

    public void setPrimaryWindow(Window primaryWindow) {
        this.primaryWindow = primaryWindow;

        primaryWindow.sizeToScene();
    }

    // Runs at the start of the graphical application
    public void initialize(){
        programsChoiceBox.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
            if(newValue == null) return;

            chosenProgram = newValue;

            clearTableView();

            populateTreeView(newValue);
        });

        treeView.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newItem) -> {
            if (newItem != null) {
                clearTableView();

                Map<String, List<TableDisplayable>> displayableData = controller.getAllDisplayableData(chosenProgram);

                List<TableDisplayable> dataToDisplay = displayableData.get(newItem.getValue());

                if(dataToDisplay == null || dataToDisplay.isEmpty()) {
                    return;
                }

                List<String> columnNames = dataToDisplay.get(0).getColumnNames();
                for (int colIndex = 0; colIndex < columnNames.size(); colIndex++) {
                    TableColumn<TableDisplayable, String> column = new TableColumn<>(columnNames.get(colIndex));

                    final int index = colIndex;
                    column.setCellValueFactory(cellData -> {
                        Object value = cellData.getValue().getColumnValues().get(index);
                        return new SimpleStringProperty(value != null ? value.toString() : "");
                    });

                    tableView.getColumns().add(column);
                }

                tableView.getItems().addAll(dataToDisplay);
            }
        });

        // Add the timetable visualization
        tableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Object selectedItem = tableView.getSelectionModel().getSelectedItem();
                if (selectedItem instanceof Timetable) {
                    Timetable timetable = (Timetable) selectedItem;

                    showTimetable(controller.getDataRepository(chosenProgram), timetable);
                }
            }
        });

        generalConfiguration = ConfigurationManager.loadConfig();

        if(generalConfiguration.getShowTutorial()) {
            showTutorial();
            generalConfiguration.tutorialShown();
        }
    }

    // Runs at the end of the graphical application
    public void cleanup() {
        System.out.println("Closing application...");
        activeTimelines.forEach((p, t) -> stopAndClearTimeline(t));
        controller.cleanup();
    }
}
