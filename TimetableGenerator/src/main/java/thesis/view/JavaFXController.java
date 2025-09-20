package thesis.view;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.util.Duration;
import thesis.controller.ControllerInterface;
import thesis.model.domain.InMemoryRepository;
import thesis.model.domain.components.Timetable;
import thesis.model.domain.components.TableDisplayable;
import thesis.utils.DoubleToolkit;
import thesis.view.managers.ConfigurationManager;
import thesis.view.managers.ProgressBarManager;
import thesis.view.managers.components.GeneralConfiguration;
import thesis.view.managers.WindowManager;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class JavaFXController implements ViewInterface {
    private static final int PROGRESSBAR_UPDATE_SECONDS = 2;

    private WindowManager windowManager;
    private ControllerInterface controller;
    private boolean isConnected = false;
    private volatile String chosenProgram;
    private Window primaryWindow;
    private GeneralConfiguration generalConfiguration;
    private ProgressBarManager progressBarManager;

    @FXML
    private Pane applicationPrincipalPane;

    @FXML
    private Pane dragAndDropPane;

    @FXML
    private TextField ipField;

    @FXML
    private TextField portField;

    @FXML
    private TextField usernameField;

    @FXML
    private TextField passwordField;

    @FXML
    private TreeView<String> treeView;

    @FXML
    private Button connectBtn;

    @FXML
    private VBox taskContainer;

    @FXML
    private ListView<HBox> progressContainer;

    @FXML
    private ChoiceBox<String> programsChoiceBox;

    @FXML
    private TableView<TableDisplayable> tableView;

    @FXML
    private Button reoptimizeButton;

    @FXML
    private Button removeButton;

    @Override
    public void setController(ControllerInterface controller) {
        this.controller = controller;
    }

    @FXML
    private void connectEvent() {
        if (!isConnected) {
            if(ipField.getText().isEmpty()) {
                showErrorAlert("The ip field should not be empty");
                return;
            }
            if(portField.getText().isEmpty()) {
                showErrorAlert("The port field should not be empty");
                return;
            }
            if(usernameField.getText().isEmpty()) {
                showErrorAlert("The username field should not be empty");
                return;
            }
            if(passwordField.getText().isEmpty()) {
                showErrorAlert("The password field should not be empty");
                return;
            }

            try {
                connectToDB();

                isConnected = true;
            } catch (Exception e) {
                showExceptionMessage(e);
            }
        } else {
            disconnectFromDB();
            isConnected = false;
        }

        ipField.setDisable(isConnected);
        portField.setDisable(isConnected);
        usernameField.setDisable(isConnected);
        passwordField.setDisable(isConnected);

        connectBtn.setText(isConnected ? "Disconnect" : "Connect");
    }

    private void connectToDB() throws Exception {
        String ip = ipField.getText();
        String port = portField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();

        //TODO: Sanitize the user inputs

        controller.connectToDatabase(ip, port, username, password);

        generalConfiguration.setIpField(ip);
        generalConfiguration.setPortField(port);
        generalConfiguration.setUsernameField(username);
    }

    private void disconnectFromDB() {
        controller.disconnectFromDatabase();
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
        Set<String> oldItems = new HashSet<>(programsChoiceBox.getItems());
        Set<String> storedPrograms = controller.getStoredPrograms();

        if(oldItems.containsAll(storedPrograms)) {
            // Nothing changed
            return;
        }

        programsChoiceBox.setItems(FXCollections.observableList(new ArrayList<>(storedPrograms)));

        // Choose the first item not present on the list previous to the update
        for(String item : storedPrograms) {
            if(!oldItems.contains(item)) {
                programsChoiceBox.setValue(item);
                break;
            }
        }
    }

    @FXML
    private void configMenuEvent() {
        windowManager.getConfigWindow(generalConfiguration).show();
    }

    private boolean checkBeforeExportation() {
        if(chosenProgram == null) {
            showErrorAlert("A program must be chosen first!");
            return false;
        }

        return true;
    }

    @FXML
    private void exportDataITCEvent() {
        if(!checkBeforeExportation()) {
            return;
        }

        controller.exportDataToITC(chosenProgram);
    }

    @FXML
    private void exportDataCSVEvent() {
        if(!checkBeforeExportation()) {
            return;
        }

        controller.exportToCSV(chosenProgram);
    }

    @FXML
    private void exportSolutionsITCEvent() {
        if(!checkBeforeExportation()) {
            return;
        }

        controller.exportSolutionsToITC(chosenProgram);
    }

    @FXML
    private void exportSolutionsPNGEvent() {
        if(!checkBeforeExportation()) {
            return;
        }

        controller.exportToPNG(chosenProgram);
    }

    @FXML
    private void exportSolutionsPDFEvent() {
        if(!checkBeforeExportation()) {
            return;
        }

        controller.exportToPDF(chosenProgram);
    }

    @FXML
    private void dragOverEvent(DragEvent event) {
        event.acceptTransferModes(TransferMode.MOVE);

        event.consume();
    }

    @FXML
    private void dragEnteredEvent(DragEvent event) {
        if (event.getGestureSource() != applicationPrincipalPane &&
                event.getDragboard().hasFiles()) {
            dragAndDropPane.setVisible(true);
        }

        event.consume();
    }

    @FXML
    private void dragExitedEvent(DragEvent event) {
        dragAndDropPane.setVisible(false);

        event.consume();
    }

    @FXML
    private void dragDroppedEvent(DragEvent event) {
        Dragboard dragboard = event.getDragboard();
        if(dragboard.hasFiles()) {
            List<File> files = dragboard.getFiles();

            for(File file : files) {
                controller.importITCData(file);
            }

            updateStoredPrograms();
        }

        event.consume();
    }

    @FXML
    private void generateSolutionEvent() {
        if(chosenProgram == null) {
            showErrorAlert("A program must be chosen before starting the generation of a solution!");
            return;
        }

        UUID progressBarUUID = progressBarManager.insertProgressBar(chosenProgram);
        progressBarManager.setCancelAction(progressBarUUID, (event) -> {
            controller.cancelGeneration(progressBarUUID);
            progressBarManager.stopAndClearTimeline(progressBarUUID);
        });

        controller.startGeneratingSolution(chosenProgram,
                progressBarUUID,
                generalConfiguration.getInitialSolutionMaxIterations(),
                generalConfiguration.getInitialTemperature(),
                generalConfiguration.getMinTemperature(),
                generalConfiguration.getCoolingRate(),
                generalConfiguration.getK());

        KeyFrame keyframe = new KeyFrame(Duration.seconds(PROGRESSBAR_UPDATE_SECONDS), e -> progressBarUpdate(e, progressBarUUID));
        progressBarManager.startTimeline(progressBarUUID, Timeline.INDEFINITE, keyframe);
    }

    private void progressBarUpdate(ActionEvent actionEvent, UUID progressBarUUID) {
        try {
            double progress = controller.getGenerationProgress(progressBarUUID);

            progressBarManager.setProgress(progressBarUUID, progress);

            if (DoubleToolkit.isEqual(progress, 1)) {
                String programName = progressBarManager.getProgramName(progressBarUUID);
                progressBarManager.stopAndClearTimeline(progressBarUUID);

                if(programName.equals(chosenProgram)) {
                    populateTreeView(programName);
                }

                showInformationAlert("The solution for the program " + programName + " has been created!\nPerform a double click on it to visualize!");

                updateTableView();
            }
        } catch (Exception e) {
            // This should only happen if the process of storing the result is interrupted
            // but that only happens if the progress is 100%, so it should be impossible
            progressBarManager.stopAndClearTimeline(progressBarUUID);
            controller.cancelGeneration(progressBarUUID);
            showExceptionMessage(e);
        } finally {
            actionEvent.consume();
        }
    }

    @FXML
    private void removeEvent() {
        Object selectedItem = tableView.getSelectionModel().getSelectedItem();
        if (selectedItem instanceof Timetable) {
            Timetable timetable = (Timetable) selectedItem;
            controller.removeTimetable(timetable);

            updateTableView();
        }
    }

    @FXML
    private void reoptimizeEvent() {
        Object selectedItem = tableView.getSelectionModel().getSelectedItem();
        if (selectedItem instanceof Timetable) {
            Timetable timetable = (Timetable) selectedItem;
            //controller.optimizeTimetable(timetable);
        }
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

    @FXML
    public void showInstructionsMenuEvent() {
        windowManager.getInstructionsWindow().show();
    }

    @Override
    public void showInformationAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
        alert.initStyle(StageStyle.DECORATED);
        alert.initOwner(primaryWindow);
        alert.initModality(Modality.WINDOW_MODAL);

        alert.show();
    }

    @Override
    public void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.initStyle(StageStyle.DECORATED);
        alert.initOwner(primaryWindow);
        alert.initModality(Modality.WINDOW_MODAL);

        alert.show();
    }

    @Override
    public boolean showConfirmationAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message);
        alert.initStyle(StageStyle.DECORATED);
        alert.initOwner(primaryWindow);
        alert.initModality(Modality.WINDOW_MODAL);

        alert.getButtonTypes().clear();
        alert.getButtonTypes().add(ButtonType.YES);
        alert.getButtonTypes().add(ButtonType.NO);

        Optional<ButtonType> optionalResponse = alert.showAndWait();

        if(optionalResponse.isPresent()) {
            ButtonType response = optionalResponse.get();

            return response.equals(ButtonType.YES);
        }

        return false;
    }

    public void showTimetable(InMemoryRepository data, Timetable timetable) {
        windowManager.getTimetableWindow(data, timetable, generalConfiguration).show();
    }

    @Override
    public void showExceptionMessage(Exception e) {
        windowManager.getExceptionMessage(e).showAndWait();
    }

    @Override
    public void setPrimaryWindow(Window primaryWindow) {
        this.primaryWindow = primaryWindow;
    }

    private void clearTableView() {
        tableView.getColumns().clear();
        tableView.getItems().clear();
    }

    private void updateTableView() {
        // Update the selected table view of the current item
        updateTableView(treeView.getSelectionModel().getSelectedItem());
    }

    private void updateTableView(TreeItem<String> item) {
        if (item != null) {
            clearTableView();

            Map<String, List<TableDisplayable>> displayableData = controller.getAllDisplayableData(chosenProgram);

            List<TableDisplayable> dataToDisplay = displayableData.get(item.getValue());

            if(dataToDisplay == null || dataToDisplay.isEmpty()) {
                return;
            }

            TableDisplayable firstElement = dataToDisplay.get(0);
            List<String> columnNames = firstElement.getColumnNames();
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

            removeButton.setVisible(firstElement.isRemovable());
            reoptimizeButton.setVisible(firstElement.isOptimizable());
        }
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

        treeView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldItem, newItem) -> updateTableView(newItem));

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

        windowManager = new WindowManager(primaryWindow);
        try {
            generalConfiguration = ConfigurationManager.loadConfig();
        } catch (IOException e) {
            showExceptionMessage(e);
            System.exit(1);
        }

        // Set the stored values for all the fields
        ipField.setText(generalConfiguration.getIpField());
        portField.setText(generalConfiguration.getPortField());
        usernameField.setText(generalConfiguration.getUsernameField());

        progressBarManager = new ProgressBarManager(progressContainer);

        // Only show the taskContainer if there are any tasks
        taskContainer.managedProperty().bind(taskContainer.visibleProperty());
        taskContainer.visibleProperty().bind(
                Bindings.isNotEmpty(progressContainer.getItems())
        );

        // Update the window size if the number of tasks change
        progressContainer.getItems().addListener((ListChangeListener<HBox>) c -> {
            primaryWindow.sizeToScene();
        });

        // Show the instructions if it's the first execution of the application
        if(generalConfiguration.getShowInstructions()) {
            showInstructionsMenuEvent();
            generalConfiguration.setShowInstructions(false);
        }
    }

    @Override
    public void cleanup() {
        // Only update the config file if the configuration was changed
        if(generalConfiguration.getUpdateConfigFile()) {
            try {
                ConfigurationManager.saveConfig(generalConfiguration);
            } catch (IOException e) {
                showExceptionMessage(e);
            }
        }

        progressBarManager.stopAndClearTimelines();
    }
}
