package thesis.view;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import thesis.controller.ControllerInterface;
import thesis.model.domain.InMemoryRepository;
import thesis.model.domain.components.Timetable;
import thesis.model.exceptions.CheckedIllegalStateException;
import thesis.model.exceptions.InvalidConfigurationException;
import thesis.model.exceptions.ParsingException;
import thesis.view.managers.ConfigurationManager;
import thesis.view.managers.ProgressBarManager;
import thesis.view.managers.components.GeneralConfiguration;
import thesis.view.managers.WindowManager;
import thesis.view.viewobjects.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class JavaFXController implements ViewInterface {
    private WindowManager windowManager;
    private ControllerInterface controller;
    private boolean isConnected = false;
    private volatile String chosenProgram;
    private Window primaryWindow;
    private GeneralConfiguration generalConfiguration;
    private ProgressBarManager progressBarManager;

    // UI Components
    @FXML private Pane applicationPrincipalPane;
    @FXML private Pane dragAndDropPane;
    @FXML private TextField ipField;
    @FXML private TextField portField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TreeView<TableType> treeView;
    @FXML private Button connectBtn;
    @FXML private VBox taskContainer;
    @FXML private ListView<HBox> progressContainer;
    @FXML private ChoiceBox<String> programsChoiceBox;
    @FXML private TableView<ViewModel> tableView;
    @FXML private Button reoptimizeButton;
    @FXML private Button removeButton;

    // Types of values that can be shown on the table view
    public enum TableType {
        CLASSES {
            @Override
            public String toString() {return "Classes";}
        },
        CONFIGURATION {
            @Override
            public String toString() {return "Configuration";}
        },
        CONFIG {
            @Override
            public String toString() {return "Configs";}
        },
        CONSTRAINT {
            @Override
            public String toString() {return "Constraints";}
        },
        COURSE {
            @Override
            public String toString() {return "Courses";}
        },
        ROOM {
            @Override
            public String toString() {return "Rooms";}
        },
        SUBPART {
            @Override
            public String toString() {return "Subparts";}
        },
        TIMETABLE {
            @Override
            public String toString() {return "Timetables";}
        }
    }

    @Override
    public void setController(ControllerInterface controller) {
        this.controller = controller;

        progressBarManager = new ProgressBarManager(progressContainer, controller, this);
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

    private void importData(List<File> files) {
        Task<Void> importTask = new Task<>() {
            @Override
            protected Void call() throws CheckedIllegalStateException, InvalidConfigurationException, ParsingException {
            for(File file : files) {
                controller.importITCData(file);
            }

            return null;
            }
        };

        importTask.setOnSucceeded(event -> {
            updateStoredPrograms();
        });

        importTask.setOnFailed(event -> {
            showExceptionMessage(event.getSource().getException());
        });

        new Thread(importTask).start();
    }

    @FXML
    private void importDataITCEvent() {
        FileChooser fileChooser = createXmlFileChooser();

        List<File> chosenFiles = fileChooser.showOpenMultipleDialog(new Stage());

        if(chosenFiles != null) {
            importData(chosenFiles);
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
            importData(files);
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

        controller.startGeneratingSolution(chosenProgram,
                                    progressBarUUID,
                                    generalConfiguration.getInitialTemperature(),
                                    generalConfiguration.getMinTemperature(),
                                    generalConfiguration.getCoolingRate(),
                                    generalConfiguration.getK());

        progressBarManager.startProgressBar(progressBarUUID);
    }

    @FXML
    private void removeEvent() {
        Object selectedItem = tableView.getSelectionModel().getSelectedItem();
        if (selectedItem instanceof TimetableViewModel) {
            TimetableViewModel timetableViewModel = (TimetableViewModel) selectedItem;
            windowManager.removeTimetableCache(timetableViewModel.getTimetable());
            controller.removeTimetable(timetableViewModel.getTimetable());

            updateTableView();
        }
    }

    @FXML
    private void reoptimizeEvent() {
        Object selectedItem = tableView.getSelectionModel().getSelectedItem();
        if (selectedItem instanceof Timetable) {
            Timetable timetable = (Timetable) selectedItem;
            //controller.optimizeTimetable(timetable);
            //TODO: to be implemented
        }
    }

    @FXML
    public void showInstructionsMenuEvent() {
        windowManager.getInstructionsWindow().show();
    }

    private static void runOnFxThread(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }

    @Override
    public void showInformationAlert(String message) {
        runOnFxThread(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
            alert.initStyle(StageStyle.DECORATED);
            alert.initOwner(primaryWindow);
            alert.initModality(Modality.WINDOW_MODAL);

            alert.show();
        });
    }

    @Override
    public void showErrorAlert(String message) {
        runOnFxThread(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, message);
            alert.initStyle(StageStyle.DECORATED);
            alert.initOwner(primaryWindow);
            alert.initModality(Modality.WINDOW_MODAL);

            alert.show();
        });
    }

    @Override
    public boolean showConfirmationAlert(String message) {
        if (Platform.isFxApplicationThread()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
            alert.initStyle(StageStyle.DECORATED);
            alert.initOwner(primaryWindow);
            alert.initModality(Modality.WINDOW_MODAL);

            Optional<ButtonType> optionalResponse = alert.showAndWait();

            if(optionalResponse.isPresent()) {
                ButtonType response = optionalResponse.get();

                return response.equals(ButtonType.YES);
            }
        } else {
            final Object lock = new Object();
            final AtomicReference<Optional<ButtonType>> resultRef = new AtomicReference<>();

            // Run the following code on the JavaFX thread
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
                alert.initStyle(StageStyle.DECORATED);
                alert.initOwner(primaryWindow);
                alert.initModality(Modality.WINDOW_MODAL);

                resultRef.set(alert.showAndWait());

                synchronized (lock) {
                    lock.notify();
                }
            });

            // Synchronize the JavaFX thread with the current thread
            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            Optional<ButtonType> optionalResponse = resultRef.get();

            if(optionalResponse.isPresent()) {
                ButtonType response = optionalResponse.get();

                return response.equals(ButtonType.YES);
            }
        }

        return false;
    }

    public void showTimetable(InMemoryRepository data, Timetable timetable) {
        windowManager.getTimetableWindow(data, timetable).show();
    }

    @Override
    public void showExceptionMessage(Throwable e) {
        windowManager.getExceptionMessage(e).show();
    }

    @Override
    public void setPrimaryWindow(Window primaryWindow) {
        this.primaryWindow = primaryWindow;
    }

    private void clearTableView() {
        tableView.getColumns().clear();
        tableView.getItems().clear();
    }

    @Override
    public void updateTableView() {
        // Update the selected table view of the current item
        updateTableView(treeView.getSelectionModel().getSelectedItem());
    }

    @SafeVarargs
    private void setTableViewData(ObservableList<ViewModel> rowData, TableColumn<ViewModel, ?> ... columnData) {
        tableView.getColumns().setAll(FXCollections.observableArrayList(columnData));
        tableView.setItems(rowData);
    }

    private void updateTableView(TreeItem<TableType> item) {
        clearTableView();
        removeButton.setVisible(false);
        reoptimizeButton.setVisible(false);

        if (item != null && chosenProgram != null) {
            final String chosenProgramStr = chosenProgram;

            switch(item.getValue()) {
                case CONFIGURATION:
                    TableColumn<ViewModel, Number> nDays = new TableColumn<>("Number of Days");
                    TableColumn<ViewModel, Number> nWeeks = new TableColumn<>("Number of Weeks");
                    TableColumn<ViewModel, Number> nSlots = new TableColumn<>("Slots per Day");
                    TableColumn<ViewModel, Number> timeWeight = new TableColumn<>("Time Weight");
                    TableColumn<ViewModel, Number> roomWeight = new TableColumn<>("Room Weight");
                    TableColumn<ViewModel, Number> distributionWeight = new TableColumn<>("Distribution Weight");

                    nDays.setCellValueFactory(data -> ((ConfigurationViewModel) data.getValue()).nDaysProperty());
                    nWeeks.setCellValueFactory(data -> ((ConfigurationViewModel) data.getValue()).nWeeksProperty());
                    nSlots.setCellValueFactory(data -> ((ConfigurationViewModel) data.getValue()).nSlotsProperty());
                    timeWeight.setCellValueFactory(data -> ((ConfigurationViewModel) data.getValue()).timeWeightProperty());
                    roomWeight.setCellValueFactory(data -> ((ConfigurationViewModel) data.getValue()).roomWeightProperty());
                    distributionWeight.setCellValueFactory(data -> ((ConfigurationViewModel) data.getValue()).distributionWeightProperty());

                    setTableViewData(controller.getConfiguration(chosenProgramStr), nDays, nWeeks, nSlots, timeWeight, roomWeight, distributionWeight);
                    break;
                case COURSE:
                    TableColumn<ViewModel, String> courseId = new TableColumn<>("Course Id");
                    TableColumn<ViewModel, Number> nConfigs = new TableColumn<>("Nº of Configs");

                    courseId.setCellValueFactory(data -> ((CourseViewModel) data.getValue()).idProperty());
                    nConfigs.setCellValueFactory(data -> ((CourseViewModel) data.getValue()).nConfigsProperty());

                    setTableViewData(controller.getCourses(chosenProgramStr), courseId, nConfigs);
                    break;
                case CONFIG:
                    TableColumn<ViewModel, String> configId = new TableColumn<>("Config Id");
                    TableColumn<ViewModel, Number> nSubparts = new TableColumn<>("Nº of Subparts");

                    configId.setCellValueFactory(data -> ((ConfigViewModel) data.getValue()).idProperty());
                    nSubparts.setCellValueFactory(data -> ((ConfigViewModel) data.getValue()).nSubpartsProperty());

                    setTableViewData(controller.getConfigs(chosenProgramStr), configId, nSubparts);
                    break;
                case SUBPART:
                    TableColumn<ViewModel, String> subpartId = new TableColumn<>("Subpart Id");
                    TableColumn<ViewModel, Number> nClasses = new TableColumn<>("Nº of Classes");

                    subpartId.setCellValueFactory(data -> ((SubpartViewModel) data.getValue()).idProperty());
                    nClasses.setCellValueFactory(data -> ((SubpartViewModel) data.getValue()).nClassesProperty());

                    setTableViewData(controller.getSubparts(chosenProgramStr), subpartId, nClasses);
                    break;
                case CLASSES:
                    TableColumn<ViewModel, String> classId = new TableColumn<>("Class Id");
                    TableColumn<ViewModel, String> classParentId = new TableColumn<>("Parent Id");

                    classId.setCellValueFactory(data -> ((ClassUnitViewModel) data.getValue()).idProperty());
                    classParentId.setCellValueFactory(data -> ((ClassUnitViewModel) data.getValue()).parentIdProperty());

                    setTableViewData(controller.getClassUnits(chosenProgramStr), classId, classParentId);
                    break;
                case CONSTRAINT:
                    TableColumn<ViewModel, String> type = new TableColumn<>("Constraint Type");
                    TableColumn<ViewModel, Integer> firstParam = new TableColumn<>("First Parameter");
                    TableColumn<ViewModel, Integer> secondParam = new TableColumn<>("Second Parameter");
                    TableColumn<ViewModel, Integer> penalty = new TableColumn<>("Penalty");
                    TableColumn<ViewModel, Boolean> required = new TableColumn<>("Required");
                    TableColumn<ViewModel, Number> nConstraintClasses = new TableColumn<>("Nº of Classes");

                    type.setCellValueFactory(data -> ((ConstraintViewModel) data.getValue()).typeProperty());
                    firstParam.setCellValueFactory(data -> ((ConstraintViewModel) data.getValue()).firstParameterProperty());
                    secondParam.setCellValueFactory(data -> ((ConstraintViewModel) data.getValue()).secondParameterProperty());
                    penalty.setCellValueFactory(data -> ((ConstraintViewModel) data.getValue()).penaltyProperty());
                    required.setCellValueFactory(data -> ((ConstraintViewModel) data.getValue()).requiredProperty());
                    nConstraintClasses.setCellValueFactory(data -> ((ConstraintViewModel) data.getValue()).nClassesProperty());

                    setTableViewData(controller.getConstraints(chosenProgramStr), type, firstParam, secondParam, penalty, required, nConstraintClasses);
                    break;
                case ROOM:
                    TableColumn<ViewModel, String> roomId = new TableColumn<>("Room Id");
                    TableColumn<ViewModel, Number> roomUnavailabilities = new TableColumn<>("Nº of Unavailabilities");
                    TableColumn<ViewModel, Number> roomDistances = new TableColumn<>("Nº of Distances");

                    roomId.setCellValueFactory(data -> ((RoomViewModel) data.getValue()).idProperty());
                    roomUnavailabilities.setCellValueFactory(data -> ((RoomViewModel) data.getValue()).nUnavailabilitiesProperty());
                    roomDistances.setCellValueFactory(data -> ((RoomViewModel) data.getValue()).nDistancesProperty());

                    setTableViewData(controller.getRooms(chosenProgramStr), roomId, roomUnavailabilities, roomDistances);
                    break;
                case TIMETABLE:
                    TableColumn<ViewModel, String> dateOfCreation = new TableColumn<>("Date of Creation");
                    TableColumn<ViewModel, Number> runtime = new TableColumn<>("Runtime");
                    TableColumn<ViewModel, Number> cost = new TableColumn<>("Cost");
                    TableColumn<ViewModel, Number> nScheduledLessons = new TableColumn<>("Nº of Scheduled Lessons");
                    TableColumn<ViewModel, Boolean> isValid = new TableColumn<>("is Valid");

                    dateOfCreation.setCellValueFactory(data -> ((TimetableViewModel) data.getValue()).dateOfCreationProperty());
                    runtime.setCellValueFactory(data -> ((TimetableViewModel) data.getValue()).runtimeProperty());
                    cost.setCellValueFactory(data -> ((TimetableViewModel) data.getValue()).costProperty());
                    nScheduledLessons.setCellValueFactory(data -> ((TimetableViewModel) data.getValue()).nScheduledClassesProperty());
                    isValid.setCellValueFactory(data -> ((TimetableViewModel) data.getValue()).isValidProperty());

                    setTableViewData(controller.getTimetables(chosenProgramStr), dateOfCreation, runtime, cost, nScheduledLessons, isValid);

                    removeButton.setVisible(true);
                    //reoptimizeButton.setVisible(true);
                    break;
            }
        }
    }

    private void populateTreeView() {
        TreeItem<TableType> root = new TreeItem<>(null);

        TreeItem<TableType> constraints = new TreeItem<>(TableType.CONSTRAINT);
        TreeItem<TableType> configuration = new TreeItem<>(TableType.CONFIGURATION);
        TreeItem<TableType> courses = new TreeItem<>(TableType.COURSE);
        TreeItem<TableType> configs = new TreeItem<>(TableType.CONFIG);
        TreeItem<TableType> subparts = new TreeItem<>(TableType.SUBPART);
        TreeItem<TableType> classes = new TreeItem<>(TableType.CLASSES);
        TreeItem<TableType> rooms = new TreeItem<>(TableType.ROOM);
        TreeItem<TableType> timetables = new TreeItem<>(TableType.TIMETABLE);

        ObservableList<TreeItem<TableType>> rootChildren = root.getChildren();
        rootChildren.addAll(List.of(configuration, courses, configs, subparts, classes, constraints, rooms, timetables));
        treeView.setRoot(root);
    }

    // Runs at the start of the graphical application
    public void initialize(){
        // Updates the String of the chosen program and clears the table when a program is chosen
        programsChoiceBox.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
            if(newValue == null) return;

            chosenProgram = newValue;

            // Clear the table and selected item on the left
            treeView.getSelectionModel().clearSelection();
            clearTableView();
        });

        // Update the table based on the category chosen on the left menu
        treeView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldItem, newItem) -> updateTableView(newItem));

        // Add the timetable visualization when the item is double-clicked
        tableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Object selectedItem = tableView.getSelectionModel().getSelectedItem();
                if (selectedItem instanceof TimetableViewModel) {
                    TimetableViewModel timetableViewModel = (TimetableViewModel) selectedItem;

                    showTimetable(controller.getDataRepository(chosenProgram), timetableViewModel.getTimetable());
                }
            }
        });

        try {
            generalConfiguration = ConfigurationManager.loadConfig();
        } catch (IOException e) {
            showExceptionMessage(e);
            System.exit(1);
        }
        windowManager = new WindowManager(primaryWindow, this, generalConfiguration);

        // Populates the left menu with the possible options
        populateTreeView();

        // Set the stored values for all the fields
        ipField.setText(generalConfiguration.getIpField());
        portField.setText(generalConfiguration.getPortField());
        usernameField.setText(generalConfiguration.getUsernameField());

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

        progressBarManager.stopProgressBars();
    }
}
