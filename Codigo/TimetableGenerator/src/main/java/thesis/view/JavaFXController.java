package thesis.view;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import thesis.controller.ControllerInterface;

import java.io.File;
import java.util.Arrays;

public class JavaFXController implements ViewInterface {
    private ControllerInterface controller;
    private boolean isConnected = false;

    @Override
    public void setController(ControllerInterface controller) {
        this.controller = controller;
    }

    @FXML
    private TextField ipField;

    @FXML
    private TreeView<String> treeView;

    @FXML
    private TextField portField;

    @FXML
    private Button connectBtn;

    @FXML
    private ProgressBar progressBar;

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
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("xml files", ".xml"));

        File chosenFile = fileChooser.showOpenDialog(new Stage());

        controller.importITCData(chosenFile);
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
        progressBar.setVisible(true);

        // TODO: Gerar soluções e apresentar progresso
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

    public void showErrorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);

        alert.showAndWait();
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

    // Runs at the start of the graphical application
    public void initialize(){
        progressBar.setVisible(false);

        populateTreeView();
    }
}
