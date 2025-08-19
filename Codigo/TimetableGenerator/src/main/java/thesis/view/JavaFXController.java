package thesis.view;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import thesis.controller.ControllerInterface;
import thesis.model.domain.*;

import java.io.File;
import java.util.*;

public class JavaFXController implements ViewInterface {
    private static final int TIMELINE_UPDATE_TIME_SECONDS = 2;
    private ControllerInterface controller = null;
    private boolean isConnected = false;
    private String chosenProgram = null;
    private Window primaryWindow = null;
    private final Map<String, ProgressBar> progressBarMap = new HashMap<>();      // ProgramName : ProgressBar
    private final Map<String, Timeline> activeTimelines = new HashMap<>();        // ProgramName : Timeline

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

    @FXML
    private void importMenuITCEvent() {
        FileChooser fileChooser = new FileChooser();

        // Sets the initial location of the FileChooser as the directory where the application is located
        File initialDirectory = new File(String.valueOf(ClassLoader.getSystemResource("."))).getAbsoluteFile().getParentFile();
        if(initialDirectory != null && initialDirectory.isDirectory()) {
            fileChooser.setInitialDirectory(initialDirectory);
        }

        FileChooser.ExtensionFilter xmlFileFilter = new FileChooser.ExtensionFilter("XML Files", "*.xml");
        fileChooser.getExtensionFilters().add(xmlFileFilter);
        fileChooser.setSelectedExtensionFilter(xmlFileFilter);

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

        // Choose the first element if there is no program chosen
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

    }

    @FXML
    private void exportDataCSVEvent() {

    }

    @FXML
    private void exportSolutionsITCEvent() {

    }

    @FXML
    private void exportSolutionsPNGEvent() {

    }

    @FXML
    private void exportSolutionsPDFEvent() {

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

        controller.startGeneratingSolution(chosenProgram, null, 10000, 0.01, 0.001, 10);

        String generatingSolutionProgram = chosenProgram;

        Timeline progressBarUpdater = new Timeline(new KeyFrame(Duration.seconds(TIMELINE_UPDATE_TIME_SECONDS), e -> progressBarUpdate(e, parent, generatingSolutionProgram)));
        progressBarUpdater.setCycleCount(Timeline.INDEFINITE);
        progressBarUpdater.play();
        activeTimelines.put(generatingSolutionProgram, progressBarUpdater);
    }

    private Node insertProgressBar(String programName) {
        Label taskName = new Label(programName + " : ");
        ProgressBar bar = new ProgressBar();
        bar.setMaxWidth(Double.MAX_VALUE);

        HBox taskBox = new HBox(10, taskName, bar);
        HBox.setHgrow(bar, Priority.ALWAYS);

        progressContainer.getChildren().add(taskBox);

        progressBarMap.put(programName, bar);

        return taskBox;
    }

    private void progressBarUpdate(ActionEvent e, Node progressBarParent, String generatingSolutionProgram) {
        double progress = controller.getGenerationProgress(generatingSolutionProgram);

        ProgressBar progressBar = progressBarMap.get(generatingSolutionProgram);

        // Should never happen
        if(progressBar == null) {
            throw new RuntimeException("The activity should have a progress bar but it doesn't");
        }

        // A verification is made to avoid a progress bar the goes back and forth (Because the progress
        // of the initial solution is based on the unscheduled classes which can increase or decrease
        // depending on the situation)
        if(progressBar.getProgress() < progress) {
            progressBar.setProgress(progress);
        }

        // TODO: eliminar os prints de debug e concluir a apresentação do progresso da geração
        if (progress == 1.0) {
            try {
                DataRepository data = controller.getDataRepository(generatingSolutionProgram);
                Timetable timetable = controller.getGeneratedTimetable(generatingSolutionProgram);

                showTimetable(data, timetable);
            } catch (Exception ex) {
                showExceptionMessage(ex);
            }

            Timeline progressBarUpdater = activeTimelines.get(generatingSolutionProgram);

            // Should never happen
            if(progressBarUpdater == null) {
                throw new RuntimeException("The timeline for the given program name doesn't exist");
            }

            stopAndClearTimeline(progressBarUpdater);
            activeTimelines.remove(generatingSolutionProgram);

            progressBarMap.remove(generatingSolutionProgram);
            progressContainer.getChildren().remove(progressBarParent);

            showInformationMessage("The solution for the program " + generatingSolutionProgram + " has been created!");

            System.out.println("Timeline stopped itself!");
        }
    }

    private void stopAndClearTimeline(Timeline timeline) {
        timeline.stop();
        timeline.getKeyFrames().clear();
    }

    private void populateTreeView() {
        TreeItem<String> root = new TreeItem<>("Data");

        // Creation of the class hierarchy
        TreeItem<String> courseItem = new TreeItem<>("Courses");
        TreeItem<String> configItem = new TreeItem<>("Configs");
        TreeItem<String> subpartItem = new TreeItem<>("Subparts");
        TreeItem<String> classItem = new TreeItem<>("Classes");

        // Configuration of the root and children
        root.getChildren().add(courseItem);
        courseItem.getChildren().add(configItem);
        configItem.getChildren().add(subpartItem);
        subpartItem.getChildren().add(classItem);
        courseItem.setExpanded(true);
        configItem.setExpanded(true);
        subpartItem.setExpanded(true);

        TreeItem<String> teacherItem = new TreeItem<>("Teachers");
        TreeItem<String> teacherUnavailItem = new TreeItem<>("Teacher Unavailabilities");

        root.getChildren().add(teacherItem);
        teacherItem.getChildren().add(teacherUnavailItem);
        teacherItem.setExpanded(true);

        TreeItem<String> roomItem = new TreeItem<>("Rooms");
        TreeItem<String> roomUnavailItem = new TreeItem<>("Room Unavailabilities");

        root.getChildren().add(roomItem);
        roomItem.getChildren().add(roomUnavailItem);
        roomItem.setExpanded(true);

        TreeItem<String> solutionItem = new TreeItem<>("Solutions");

        root.getChildren().add(solutionItem);

        treeView.setRoot(root);
    }

    public void showInformationMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message);

        alert.show();
    }

    public void showErrorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);

        alert.showAndWait();
    }

    public void showTimetable(DataRepository data, Timetable timetable) {
        Stage stage = new Stage();

        if(primaryWindow != null) {
            stage.initOwner(primaryWindow);
            stage.initModality(Modality.WINDOW_MODAL);
        }

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(5);
        gridPane.setPadding(new Insets(10));

        HashMap<Pair<Integer, Integer>, VBox> nodesToAdd = new HashMap<>();
        int maxSlot = 0;
        int minSlot = Integer.MAX_VALUE;

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

                    VBox vBox = nodesToAdd.computeIfAbsent(new ImmutablePair<>(i,  startSlot), k -> {
                        VBox newVBox = new VBox(5);
                        newVBox.setStyle("-fx-border-color: black; -fx-padding: 5;");
                        return newVBox;
                    });

                    StringBuilder vboxLabel = new StringBuilder(scheduledLesson.getClassId() + "\n[" + data.getProgramName() + "]\n");
                    boolean addedTeacher = false;

                    List<Teacher> teacherList = scheduledLesson.getTeachers();
                    if(teacherList != null) {
                        vboxLabel.append('[');
                        for (Teacher teacher : scheduledLesson.getTeachers()) {
                            if (addedTeacher) {
                                vboxLabel.append("; ");
                            }
                            vboxLabel.append('(').append(teacher.getName()).append(')');

                            addedTeacher = true;
                        }
                        vboxLabel.append("]\n");
                    }

                    vboxLabel.append('[').append(scheduledLesson.getRoomId()).append(']');

                    vBox.getChildren().add(new Label(vboxLabel.toString()));
                }
            }
        }

        for(Map.Entry<Pair<Integer, Integer>, VBox> nodeData : nodesToAdd.entrySet()) {
            Pair<Integer, Integer> location = nodeData.getKey();
            // A value of 1 is added to the column index because column 0 is reserved for the time of the lessons
            gridPane.add(nodeData.getValue(), location.getKey()+1, location.getValue());
        }

        int numSlots = data.getTimetableConfiguration().getSlotsPerDay();
        int minutesPerSlot = 24*60/numSlots;

        for(int i = minSlot; i < maxSlot; i++) {
            int startMin = minutesPerSlot*i;
            int startHour = startMin / 60;

            int endMin = startMin + minutesPerSlot;
            int endHour = endMin / 60;

            startMin = startMin % 60;
            endMin = endMin % 60;

            String timeLabel = String.format("%02d:%02d - %02d:%02d", startHour, startMin, endHour, endMin);
            gridPane.add(new Label(timeLabel), 0, i);
        }

        ScrollPane scrollPane = new ScrollPane(gridPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        Scene scene = new Scene(scrollPane, 1000, 600); // largura x altura inicial

        stage.setScene(scene);
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
        errorStage.showAndWait();
    }

    public void setPrimaryWindow(Window primaryWindow) {
        this.primaryWindow = primaryWindow;
    }

    // Runs at the start of the graphical application
    public void initialize(){
        programsChoiceBox.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
            chosenProgram = newValue;
        });

        populateTreeView();
    }

    public void cleanup() {
        System.out.println("Closing application...");
        activeTimelines.forEach((p, t) -> stopAndClearTimeline(t));
        controller.cleanup();
    }
}
