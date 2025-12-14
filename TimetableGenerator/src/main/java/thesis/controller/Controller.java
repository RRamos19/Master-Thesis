package thesis.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.stage.*;
import org.apache.http.conn.util.InetAddressUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import thesis.model.ModelInterface;
import thesis.model.domain.InMemoryRepository;
import thesis.model.domain.components.Timetable;
import thesis.model.exceptions.CheckedIllegalArgumentException;
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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;

public class Controller implements ControllerInterface {
    private WindowManager windowManager;
    private ModelInterface model;
    private ViewInterface view;
    private boolean isConnected = false;
    private volatile String chosenProgram;
    private Window primaryWindow;
    private GeneralConfiguration generalConfiguration;
    private ProgressBarManager progressBarManager;
    private final StringProperty lastSyncText = new SimpleStringProperty("Never");

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
        },
        TEACHER {
            @Override
            public String toString() {return "Teachers";}
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

        // Validation of the inputs
        if(!ip.equalsIgnoreCase("localhost") && !InetAddressUtils.isIPv4Address(ip) && !InetAddressUtils.isIPv6Address(ip)) {
            throw new IllegalStateException("The IP address provided is neither IPv4 nor IPv6");
        }
        try {
            Integer.parseInt(port);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("The port field must be a whole number");
        }

        model.connectToDatabase(ip, port, username, password, generalConfiguration.getDatabaseSynchronizationTimeMinutes());

        generalConfiguration.setIpField(ip);
        generalConfiguration.setPortField(port);
        generalConfiguration.setUsernameField(username);
    }

    private void disconnectFromDB() {
        model.disconnectFromDatabase();
    }

    private FileChooser createImportFileChooser() {
        FileChooser fileChooser = new FileChooser();

        // Sets the initial location of the FileChooser as the directory where the application is located
        File initialDirectory = new File(String.valueOf(ClassLoader.getSystemResource("."))).getAbsoluteFile().getParentFile();
        if(initialDirectory != null && initialDirectory.isDirectory()) {
            fileChooser.setInitialDirectory(initialDirectory);
        }

        FileChooser.ExtensionFilter xmlFileFilter = new FileChooser.ExtensionFilter("XML and Excel Files", "*.xml", "*.xlsx");
        fileChooser.getExtensionFilters().add(xmlFileFilter);
        fileChooser.setSelectedExtensionFilter(xmlFileFilter);

        return fileChooser;
    }

    private void importData(List<File> files) {
        Task<Void> importTask = new Task<>() {
            @Override
            protected Void call() throws CheckedIllegalStateException, InvalidConfigurationException, ParsingException, IOException, CheckedIllegalArgumentException {
                for(File file : files) {
                    importDataFromFile(file);
                }

                return null;
            }
        };

        importTask.setOnSucceeded(event -> {
            updateStoredPrograms();
            updateTableView();
        });

        importTask.setOnFailed(event -> {
            showExceptionMessage(event.getSource().getException());
        });

        new Thread(importTask).start();
    }

    private void importDataFromFile(File file) throws CheckedIllegalStateException, InvalidConfigurationException, ParsingException, IOException, CheckedIllegalArgumentException {
        XmlResult result;

        result = model.readFile(file);

        if(result instanceof InMemoryRepository) {
            InMemoryRepository dataRepository = (InMemoryRepository) result;
            String programName = dataRepository.getProgramName();
            if(programName == null) {
                showErrorAlert("Program has no name!");
                return;
            }

            InMemoryRepository storedData = model.getDataRepository(programName);

            if(storedData == null) {
                model.importRepository(dataRepository);
            } else {
                showConfirmationAlert("There is already a program stored which is equal to the program of the file provided. Overwrite (while retaining the solutions, if possible) ?", (confirmation) -> {
                    if(confirmation) {
                        for(Timetable solution : storedData.getTimetableList()) {
                            solution.clearCache();
                            try {
                                dataRepository.addTimetable(solution);
                            } catch (InvalidConfigurationException ignored) {}
                        }

                        try {
                            model.importRepository(dataRepository);
                        } catch (InvalidConfigurationException ignored) {}
                    }
                });
            }
        } else if(result instanceof Timetable) {
            Timetable solution = (Timetable) result;
            model.importSolution(solution);
        } else {
            throw new IllegalStateException("The resulting type of the parsing is unsupported");
        }
    }

    @Override
    public StringProperty getLastSyncStringProperty() {
        return lastSyncText;
    }

    @Override
    public void updateLastSyncText() {
        Platform.runLater(() -> {
            lastSyncText.setValue(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        });
    }

    @Override
    public void importDataEvent() {
        FileChooser fileChooser = createImportFileChooser();

        List<File> chosenFiles = fileChooser.showOpenMultipleDialog(new Stage());

        if(chosenFiles != null) {
            importData(chosenFiles);
        }
    }

    @Override
    public void updateStoredPrograms() {
        Platform.runLater(() -> {
            ChoiceBox<String> programsChoiceBox = view.getProgramsChoiceBox();

            Set<String> oldItems = new HashSet<>(programsChoiceBox.getItems());
            Set<String> storedPrograms = model.getStoredPrograms();

            if(oldItems.size() == storedPrograms.size() &&
                    oldItems.containsAll(storedPrograms)) {
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
        });
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

        export(chosenProgram, ModelInterface.ExportType.DATA_ITC);
    }

    @Override
    public void exportDataXLSXEvent() {
        if(!checkBeforeExportation()) {
            return;
        }

        export(chosenProgram, ModelInterface.ExportType.XLSX);
    }

    @Override
    public void exportSolutionITCEvent() {
        if(!checkBeforeExportation()) {
            return;
        }

        Object selectedItem = view.getTableView().getSelectionModel().getSelectedItem();
        if (selectedItem instanceof TimetableViewModel) {
            TimetableViewModel timetableViewModel = (TimetableViewModel) selectedItem;

            export(timetableViewModel.getTimetable(), ModelInterface.ExportType.SOLUTIONS_ITC);
        } else {
            showErrorAlert("A timetable must be chosen before exporting to ITC format");
        }
    }

    @Override
    public void exportSolutionPNGEvent() {
        if(!checkBeforeExportation()) {
            return;
        }

        Object selectedItem = view.getTableView().getSelectionModel().getSelectedItem();
        if (selectedItem instanceof TimetableViewModel) {
            TimetableViewModel timetableViewModel = (TimetableViewModel) selectedItem;

            export(timetableViewModel.getTimetable(), generalConfiguration.getMaxHour(), generalConfiguration.getMinHour(), ModelInterface.ExportType.PNG);
        } else {
            showErrorAlert("A timetable must be chosen before exporting to PNG format");
        }
    }

    @Override
    public void exportSolutionPDFEvent() {
        if(!checkBeforeExportation()) {
            return;
        }

        Object selectedItem = view.getTableView().getSelectionModel().getSelectedItem();
        if (selectedItem instanceof TimetableViewModel) {
            TimetableViewModel timetableViewModel = (TimetableViewModel) selectedItem;

            export(timetableViewModel.getTimetable(), generalConfiguration.getMaxHour(), generalConfiguration.getMinHour(), ModelInterface.ExportType.PDF);
        } else {
            showErrorAlert("A timetable must be chosen before exporting to PDF format");
        }
    }

    private void export(String programName, ModelInterface.ExportType type) {
        Task<Void> exportTask = new Task<>() {
            @Override
            protected Void call() throws IOException, InvalidFormatException {
                model.export(programName, type);

                return null;
            }
        };

        exportTask.setOnSucceeded((event) -> showInformationAlert("The data was exported successfully to the following location: " + model.getExportLocation()));

        exportTask.setOnFailed((event) -> showExceptionMessage(event.getSource().getException()));

        new Thread(exportTask).start();
    }

    private void export(Timetable timetable, ModelInterface.ExportType type) {
        Task<Void> exportTask = new Task<>() {
            @Override
            protected Void call() throws IOException {
                model.export(timetable, type);

                return null;
            }
        };

        exportTask.setOnSucceeded((event) -> showInformationAlert("The data was exported successfully to the following location: " + model.getExportLocation()));

        exportTask.setOnFailed((event) -> showExceptionMessage(event.getSource().getException()));

        new Thread(exportTask).start();
    }

    private void export(Timetable timetable, int maxHour, int minHour, ModelInterface.ExportType type) {
        Task<Void> exportTask = new Task<>() {
            @Override
            protected Void call() throws IOException {
                model.export(timetable, maxHour, minHour, type);

                return null;
            }
        };

        exportTask.setOnSucceeded((event) -> showInformationAlert("The data was exported successfully to the following location: " + model.getExportLocation()));

        exportTask.setOnFailed((event) -> showExceptionMessage(event.getSource().getException()));

        new Thread(exportTask).start();
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
    public void removeProgramEvent() {
        if(chosenProgram == null) {
            showErrorAlert("A program must be chosen before removal");
            return;
        }
        model.removeProgram(chosenProgram);
        chosenProgram = null;
        view.getTreeView().getSelectionModel().clearSelection();
        updateStoredPrograms();
    }

    @Override
    public void removeTableInstanceEvent() {
        Object selectedItem = view.getTableView().getSelectionModel().getSelectedItem();
        if (selectedItem instanceof TimetableViewModel) {
            TimetableViewModel timetableViewModel = (TimetableViewModel) selectedItem;
            windowManager.removeTimetableCache(timetableViewModel.getTimetable());
            model.removeTimetable(timetableViewModel.getTimetable());

            updateTableView();
        } else {
            showErrorAlert("A timetable must be chosen before removal");
        }
    }

    @Override
    public void reoptimizeSolutionEvent() {
        Object selectedItem = view.getTableView().getSelectionModel().getSelectedItem();
        if (selectedItem instanceof TimetableViewModel) {
            if(chosenProgram == null) { // Should be impossible but for security
                showErrorAlert("A program must be chosen before starting the generation of a solution!");
                return;
            }

            UUID progressBarUUID = progressBarManager.insertProgressBar(chosenProgram);

            Timetable timetable = ((TimetableViewModel) selectedItem).getTimetable();
            model.startReoptimizingSolution(timetable,
                progressBarUUID,
                generalConfiguration.getInitialTemperature(),
                generalConfiguration.getMinTemperature(),
                generalConfiguration.getCoolingRate(),
                generalConfiguration.getK());

            progressBarManager.startProgressBar(progressBarUUID);
        } else {
            showErrorAlert("A timetable must be chosen before reoptimizing");
        }
    }

    @Override
    public void showInstructionsMenuEvent() {
        windowManager.getInstructionsWindow().show();
    }

    @Override
    public void showInformationAlert(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
            alert.initStyle(StageStyle.DECORATED);
            alert.initOwner(primaryWindow);
            alert.initModality(Modality.WINDOW_MODAL);

            alert.show();
        });
    }

    @Override
    public void showErrorAlert(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, message);
            alert.initStyle(StageStyle.DECORATED);
            alert.initOwner(primaryWindow);
            alert.initModality(Modality.WINDOW_MODAL);

            alert.show();
        });
    }

    @Override
    public void showConfirmationAlert(String message, Consumer<Boolean> callback) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
            alert.initStyle(StageStyle.DECORATED);
            alert.initOwner(primaryWindow);
            alert.initModality(Modality.WINDOW_MODAL);

            Optional<ButtonType> result = alert.showAndWait();
            boolean confirmed = result.isPresent() && result.get() == ButtonType.YES;

            callback.accept(confirmed);
        });
    }

    @Override
    public void showTimetable(InMemoryRepository data, Timetable timetable) {
        Platform.runLater(() -> {
            Stage timetableWindow = windowManager.getTimetableWindow(data, timetable);
            timetableWindow.show();
        });
    }

    @Override
    public void showExceptionMessage(Throwable e) {
        Platform.runLater(() -> {
            windowManager.getExceptionMessage(e).show();
        });
    }

    @Override
    public void setPrimaryWindow(Window primaryWindow) {
        this.primaryWindow = primaryWindow;
    }

    private void clearTableView() {
        TableView<ViewModel> tableView = view.getTableView();
        if(tableView.getColumns() != null) tableView.getColumns().clear();
        if(tableView.getItems() != null) tableView.getItems().clear();
    }

    @SafeVarargs
    private void setTableViewData(ObservableList<ViewModel> rowData, TableColumn<ViewModel, ?> ... columnData) {
        TableView<ViewModel> tableView = view.getTableView();
        tableView.getColumns().setAll(FXCollections.observableArrayList(columnData));
        tableView.setItems(rowData);
    }

    @Override
    public void updateTableView() {
        // Update the selected table view of the current item
        updateTableView(view.getTreeView().getSelectionModel().getSelectedItem());
    }

    @Override
    public void updateTableView(TreeItem<TableType> item) {
        Platform.runLater(() -> {
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
                        reoptimizeButton.setVisible(true);
                        break;
                    case TEACHER:
                        TableColumn<ViewModel, Number> teacherIds = new TableColumn<>("Id");
                        TableColumn<ViewModel, String> teacherNames = new TableColumn<>("Name");
                        TableColumn<ViewModel, Number> nUnavailabilities = new TableColumn<>("Nº of Unavailabilities");
                        TableColumn<ViewModel, Number> nTeacherClasses = new TableColumn<>("Nº of Classes");

                        teacherIds.setCellValueFactory(data -> ((TeacherViewModel) data.getValue()).idProperty());
                        teacherNames.setCellValueFactory(data -> ((TeacherViewModel) data.getValue()).nameProperty());
                        nUnavailabilities.setCellValueFactory(data -> ((TeacherViewModel) data.getValue()).nUnavailabilitiesProperty());
                        nTeacherClasses.setCellValueFactory(data -> ((TeacherViewModel) data.getValue()).nClassesProperty());

                        setTableViewData(DataConverter.getTeachers(chosenProgramStr, this, model), teacherIds, teacherNames, nUnavailabilities, nTeacherClasses);
                        break;
                }
            }
        });
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
        TreeItem<TableType> teachers = new TreeItem<>(TableType.TEACHER);

        ObservableList<TreeItem<TableType>> rootChildren = root.getChildren();
        rootChildren.addAll(List.of(configuration, courses, configs, subparts, classes, constraints, rooms, teachers, timetables));
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
        model.cleanup();
    }
}
