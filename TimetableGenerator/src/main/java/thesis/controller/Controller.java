package thesis.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.stage.*;
import thesis.model.ModelInterface;
import thesis.model.domain.InMemoryRepository;
import thesis.model.domain.components.Timetable;
import thesis.model.exceptions.CheckedIllegalStateException;
import thesis.model.exceptions.InvalidConfigurationException;
import thesis.model.exceptions.ParsingException;
import thesis.model.parser.XmlResult;
import thesis.view.ViewInterface;
import thesis.controller.managers.ConfigurationManager;
import thesis.controller.managers.ProgressBarManager;
import thesis.controller.managers.components.GeneralConfiguration;
import thesis.controller.managers.WindowManager;
import thesis.view.viewobjects.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class Controller implements ControllerInterface {
    private WindowManager windowManager;
    private ModelInterface model;
    private ViewInterface view;
    private boolean isConnected = false;
    private volatile String chosenProgram;
    private Window primaryWindow;
    private GeneralConfiguration generalConfiguration;
    private ProgressBarManager progressBarManager;

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
    public void setModel(ModelInterface model) {
        this.model = model;
        model.setController(this);
    }

    @Override
    public void setView(ViewInterface view) {
        this.view = view;
    }

    @Override
    public void connectEvent() {
        if (!isConnected) {
            if(view.getIpField().getText().isEmpty()) {
                showErrorAlert("The ip field should not be empty");
                return;
            }
            if(view.getPortField().getText().isEmpty()) {
                showErrorAlert("The port field should not be empty");
                return;
            }
            if(view.getUsernameField().getText().isEmpty()) {
                showErrorAlert("The username field should not be empty");
                return;
            }
            if(view.getPasswordField().getText().isEmpty()) {
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

        view.getIpField().setDisable(isConnected);
        view.getPortField().setDisable(isConnected);
        view.getUsernameField().setDisable(isConnected);
        view.getPasswordField().setDisable(isConnected);

        view.getConnectBtn().setText(isConnected ? "Disconnect" : "Connect");
    }

    private void connectToDB() throws Exception {
        String ip = view.getIpField().getText();
        String port = view.getPortField().getText();
        String username = view.getUsernameField().getText();
        String password = view.getPasswordField().getText();

        //TODO: Sanitize the user inputs

        model.connectToDatabase(ip, port, username, password);

        generalConfiguration.setIpField(ip);
        generalConfiguration.setPortField(port);
        generalConfiguration.setUsernameField(username);
    }

    private void disconnectFromDB() {
        model.disconnectFromDatabase();
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
                importITCData(file);
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

    private void importITCData(File file) throws CheckedIllegalStateException, InvalidConfigurationException, ParsingException {
        XmlResult result;

        result = model.readFile(file);

        if(result instanceof InMemoryRepository) {
            InMemoryRepository dataRepository = (InMemoryRepository) result;

            InMemoryRepository storedData = model.getDataRepository(dataRepository.getProgramName());

            if(storedData == null) {
                model.importRepository(dataRepository);
            } else {
                if(showConfirmationAlert("There is already a program stored which is equal to the program of the file provided. Overwrite (while retaining the solutions, if possible) ?")) {
                    for(Timetable solution : storedData.getTimetableList()) {
                        dataRepository.addTimetable(solution);
                    }

                    model.importRepository(dataRepository);
                }
            }
        } else if(result instanceof Timetable) {
            Timetable solution = (Timetable) result;

            model.importSolution(solution);

        } else {
            throw new IllegalStateException("The resulting type of the parsing is unsupported");
        }
    }

    @Override
    public void importDataITCEvent() {
        FileChooser fileChooser = createXmlFileChooser();

        List<File> chosenFiles = fileChooser.showOpenMultipleDialog(new Stage());

        if(chosenFiles != null) {
            importData(chosenFiles);
        }
    }

    @Override
    public void updateStoredPrograms() {
        ChoiceBox<String> programsChoiceBox = view.getProgramsChoiceBox();

        Set<String> oldItems = new HashSet<>(programsChoiceBox.getItems());
        Set<String> storedPrograms = model.getStoredPrograms();

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

    @Override
    public void configMenuEvent() {
        windowManager.getConfigWindow(generalConfiguration).show();
    }

    private boolean checkBeforeExportation() {
        if(chosenProgram == null) {
            showErrorAlert("A program must be chosen first!");
            return false;
        }

        return true;
    }

    @Override
    public void exportDataITCEvent() {
        if(!checkBeforeExportation()) {
            return;
        }

        exportDataToITC(chosenProgram);
    }

    @Override
    public void exportDataCSVEvent() {
        if(!checkBeforeExportation()) {
            return;
        }

        exportToCSV(chosenProgram);
    }

    @Override
    public void exportSolutionsITCEvent() {
        if(!checkBeforeExportation()) {
            return;
        }

        exportSolutionsToITC(chosenProgram);
    }

    @Override
    public void exportSolutionsPNGEvent() {
        if(!checkBeforeExportation()) {
            return;
        }

        exportToPNG(chosenProgram);
    }

    @Override
    public void exportSolutionsPDFEvent() {
        if(!checkBeforeExportation()) {
            return;
        }

        exportToPDF(chosenProgram);
    }

    private void export(String programName, ModelInterface.ExportType type) {
        try {
            model.export(programName, type);
        } catch(IOException e) {
            showExceptionMessage(e);
            return;
        }

        showInformationAlert("The data was exported successfully to the following location: " + model.getExportLocation());
    }

    private void exportSolutionsToITC(String programName) {
        InMemoryRepository data = model.getDataRepository(programName);

        if(!data.getTimetableList().isEmpty()) {
            export(programName, ModelInterface.ExportType.SOLUTIONS_ITC);
        } else {
            showErrorAlert("There are no solutions to export!");
        }
    }

    private void exportDataToITC(String programName) {
        export(programName, ModelInterface.ExportType.DATA_ITC);
    }

    private void exportToCSV(String programName) {
        export(programName, ModelInterface.ExportType.CSV);
    }

    private void exportToPDF(String programName) {
        export(programName, ModelInterface.ExportType.PDF);
    }

    private void exportToPNG(String programName) {
        export(programName, ModelInterface.ExportType.PNG);
    }

    @Override
    public void dragOverEvent(DragEvent event) {
        event.acceptTransferModes(TransferMode.MOVE);
        event.consume();
    }

    @Override
    public void dragEnteredEvent(DragEvent event) {
        if (event.getGestureSource() != view.getApplicationPrincipalPane() &&
                event.getDragboard().hasFiles()) {
            view.getDragAndDropPane().setVisible(true);
        }
        event.consume();
    }

    @Override
    public void dragExitedEvent(DragEvent event) {
        view.getDragAndDropPane().setVisible(false);
        event.consume();
    }

    @Override
    public void dragDroppedEvent(DragEvent event) {
        Dragboard dragboard = event.getDragboard();
        if(dragboard.hasFiles()) {
            List<File> files = dragboard.getFiles();
            importData(files);
        }
        event.consume();
    }

    @Override
    public void generateSolutionEvent() {
        if(chosenProgram == null) {
            showErrorAlert("A program must be chosen before starting the generation of a solution!");
            return;
        }

        UUID progressBarUUID = progressBarManager.insertProgressBar(chosenProgram);

        model.startGeneratingSolution(chosenProgram,
                                    progressBarUUID,
                                    generalConfiguration.getInitialTemperature(),
                                    generalConfiguration.getMinTemperature(),
                                    generalConfiguration.getCoolingRate(),
                                    generalConfiguration.getK());

        progressBarManager.startProgressBar(progressBarUUID);
    }

    @Override
    public void removeTableInstanceEvent() {
        Object selectedItem = view.getTableView().getSelectionModel().getSelectedItem();
        if (selectedItem instanceof TimetableViewModel) {
            TimetableViewModel timetableViewModel = (TimetableViewModel) selectedItem;
            windowManager.removeTimetableCache(timetableViewModel.getTimetable());
            model.removeTimetable(timetableViewModel.getTimetable());

            updateTableView();
        }
    }

    @Override
    public void reoptimizeSolutionEvent() {
        Object selectedItem = view.getTableView().getSelectionModel().getSelectedItem();
        if (selectedItem instanceof Timetable) {
            Timetable timetable = (Timetable) selectedItem;
            //controller.optimizeTimetable(timetable);
            //TODO: to be implemented
        }
    }

    @Override
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

    @Override
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
        TableView<ViewModel> tableView = view.getTableView();
        tableView.getColumns().clear();
        tableView.getItems().clear();
    }

    @Override
    public void updateTableView() {
        // Update the selected table view of the current item
        updateTableView(view.getTreeView().getSelectionModel().getSelectedItem());
    }

    @SafeVarargs
    private void setTableViewData(ObservableList<ViewModel> rowData, TableColumn<ViewModel, ?> ... columnData) {
        TableView<ViewModel> tableView = view.getTableView();
        tableView.getColumns().setAll(FXCollections.observableArrayList(columnData));
        tableView.setItems(rowData);
    }

    @Override
    public void updateTableView(TreeItem<TableType> item) {
        Button removeButton = view.getRemoveButton();
        Button reoptimizeButton = view.getReoptimizeButton();

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

                    setTableViewData(DataConverter.getConfiguration(chosenProgramStr, this, model), nDays, nWeeks, nSlots, timeWeight, roomWeight, distributionWeight);
                    break;
                case COURSE:
                    TableColumn<ViewModel, String> courseId = new TableColumn<>("Course Id");
                    TableColumn<ViewModel, Number> nConfigs = new TableColumn<>("Nº of Configs");

                    courseId.setCellValueFactory(data -> ((CourseViewModel) data.getValue()).idProperty());
                    nConfigs.setCellValueFactory(data -> ((CourseViewModel) data.getValue()).nConfigsProperty());

                    setTableViewData(DataConverter.getCourses(chosenProgramStr, this, model), courseId, nConfigs);
                    break;
                case CONFIG:
                    TableColumn<ViewModel, String> configId = new TableColumn<>("Config Id");
                    TableColumn<ViewModel, Number> nSubparts = new TableColumn<>("Nº of Subparts");

                    configId.setCellValueFactory(data -> ((ConfigViewModel) data.getValue()).idProperty());
                    nSubparts.setCellValueFactory(data -> ((ConfigViewModel) data.getValue()).nSubpartsProperty());

                    setTableViewData(DataConverter.getConfigs(chosenProgramStr, this, model), configId, nSubparts);
                    break;
                case SUBPART:
                    TableColumn<ViewModel, String> subpartId = new TableColumn<>("Subpart Id");
                    TableColumn<ViewModel, Number> nClasses = new TableColumn<>("Nº of Classes");

                    subpartId.setCellValueFactory(data -> ((SubpartViewModel) data.getValue()).idProperty());
                    nClasses.setCellValueFactory(data -> ((SubpartViewModel) data.getValue()).nClassesProperty());

                    setTableViewData(DataConverter.getSubparts(chosenProgramStr, this, model), subpartId, nClasses);
                    break;
                case CLASSES:
                    TableColumn<ViewModel, String> classId = new TableColumn<>("Class Id");
                    TableColumn<ViewModel, String> classParentId = new TableColumn<>("Parent Id");

                    classId.setCellValueFactory(data -> ((ClassUnitViewModel) data.getValue()).idProperty());
                    classParentId.setCellValueFactory(data -> ((ClassUnitViewModel) data.getValue()).parentIdProperty());

                    setTableViewData(DataConverter.getClassUnits(chosenProgramStr, this, model), classId, classParentId);
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

                    setTableViewData(DataConverter.getConstraints(chosenProgramStr, this, model), type, firstParam, secondParam, penalty, required, nConstraintClasses);
                    break;
                case ROOM:
                    TableColumn<ViewModel, String> roomId = new TableColumn<>("Room Id");
                    TableColumn<ViewModel, Number> roomUnavailabilities = new TableColumn<>("Nº of Unavailabilities");
                    TableColumn<ViewModel, Number> roomDistances = new TableColumn<>("Nº of Distances");

                    roomId.setCellValueFactory(data -> ((RoomViewModel) data.getValue()).idProperty());
                    roomUnavailabilities.setCellValueFactory(data -> ((RoomViewModel) data.getValue()).nUnavailabilitiesProperty());
                    roomDistances.setCellValueFactory(data -> ((RoomViewModel) data.getValue()).nDistancesProperty());

                    setTableViewData(DataConverter.getRooms(chosenProgramStr, this, model), roomId, roomUnavailabilities, roomDistances);
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

                    setTableViewData(DataConverter.getTimetables(chosenProgramStr, this, model), dateOfCreation, runtime, cost, nScheduledLessons, isValid);

                    removeButton.setVisible(true);
                    //reoptimizeButton.setVisible(true);
                    break;
            }
        }
    }

    private void populateTreeView() {
        TreeView<TableType> treeView = view.getTreeView();

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

    @Override
    public void changeProgramChoiceEvent(String newValue) {
        // Updates the String of the chosen program and clears the table when a program is chosen
        if(newValue == null) return;

        chosenProgram = newValue;

        // Clear the table and selected item on the left
        view.getTreeView().getSelectionModel().clearSelection();
        clearTableView();
    }

    @Override
    public void tableViewMouseClickedEvent(MouseEvent event) {
        // Add the timetable visualization when the item is double-clicked
        if (event.getClickCount() == 2) {
            Object selectedItem = view.getTableView().getSelectionModel().getSelectedItem();
            if (selectedItem instanceof TimetableViewModel) {
                TimetableViewModel timetableViewModel = (TimetableViewModel) selectedItem;

                showTimetable(model.getDataRepository(chosenProgram), timetableViewModel.getTimetable());
            }
        }
    }

    @Override
    public void progressContainterResizeEvent() {
        primaryWindow.sizeToScene();
    }

    @Override
    public void initialize(){
        progressBarManager = new ProgressBarManager(view.getProgressContainer(), model, this);

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
        view.getIpField().setText(generalConfiguration.getIpField());
        view.getPortField().setText(generalConfiguration.getPortField());
        view.getUsernameField().setText(generalConfiguration.getUsernameField());

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
