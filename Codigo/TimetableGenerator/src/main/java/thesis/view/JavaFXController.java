package thesis.view;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.util.Duration;
import thesis.controller.ControllerInterface;
import thesis.model.domain.InMemoryRepository;
import thesis.model.domain.components.ScheduledLesson;
import thesis.model.domain.components.Timetable;
import thesis.model.domain.components.TableDisplayable;
import thesis.utils.DoubleToolkit;
import thesis.view.managers.ConfigurationManager;
import thesis.view.managers.ProgressBarManager;
import thesis.view.managers.components.GeneralConfiguration;
import thesis.view.managers.WindowManager;
import thesis.view.utils.AppIcons;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class JavaFXController implements ViewInterface {
    private static final List<String> DAYS_OF_WEEK = List.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");
    private static final int PROGRESSBAR_UPDATE_SECONDS = 2;
    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 600;

    private WindowManager windowManager;
    private ControllerInterface controller;
    private boolean isConnected = false;
    private volatile String chosenProgram;
    private Window primaryWindow;
    private GeneralConfiguration generalConfiguration;
    private ProgressBarManager progressBarManager;

    @FXML
    private BorderPane applicationPrincipalPane;

    @FXML
    private Pane dragAndDropPane;

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
                showErrorAlert("The ip field should not be empty");
                return;
            }
            if(portField.getText().isEmpty()) {
                showErrorAlert("The port field should not be empty");
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
        ObservableList<String> oldItems = programsChoiceBox.getItems();
        Set<String> storedPrograms = controller.getStoredPrograms();

        if(!oldItems.isEmpty() && storedPrograms.containsAll(oldItems)) {
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
    private void dragDetectionEvent(MouseEvent event) {
        System.out.println("Drag detected!");
        // Only accept files moved onto the application window
        applicationPrincipalPane.startDragAndDrop(TransferMode.MOVE);

        event.consume();
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
        double progress;

        try {
            progress = controller.getGenerationProgress(progressBarUUID);
        } catch (Exception e) {
            progressBarManager.stopAndClearTimeline(progressBarUUID);
            controller.cancelGeneration(progressBarUUID);
            return;
        }

        progressBarManager.setProgress(progressBarUUID, progress);

        if (DoubleToolkit.isEqual(progress, 1)) {
            String programName = progressBarManager.getProgramName(progressBarUUID);
            progressBarManager.stopAndClearTimeline(progressBarUUID);

            if(programName.equals(chosenProgram)) {
                populateTreeView(programName);
            }

            showInformationAlert("The solution for the program " + programName + " has been created!\nPerform a double click on it to visualize!");
        }

        actionEvent.consume();
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
    public void showTutorialMenuEvent() {
        windowManager.getTutorialWindow().show();
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
        Stage stage = new Stage();
        stage.getIcons().addAll(AppIcons.getAppIcons());
        stage.setTitle(timetable.getProgramName());

        if (primaryWindow != null) {
            stage.initOwner(primaryWindow);
            stage.initModality(Modality.WINDOW_MODAL);
        }

        int numSlots = data.getTimetableConfiguration().getSlotsPerDay();
        int minutesPerSlot = 24 * 60 / numSlots;
        int minHour = generalConfiguration.getMinHour();
        int maxHour = generalConfiguration.getMaxHour();

        // slots per hour and min visible slot index
        int slotsPerHour = 60 / minutesPerSlot;
        int minSlot = minHour * slotsPerHour;
        int visibleSlots = (maxHour - minHour) * slotsPerHour;

        final double TIME_COL_WIDTH = 120;
        final double DAY_SUBCOL_WIDTH = 140;
        final double ROW_HEIGHT = 40;

        // Group weeks by unique pattern of active lessons
        int maxWeeks = data.getTimetableConfiguration().getNumWeeks();

        // key -> list of week numbers
        LinkedHashMap<String, List<Integer>> weekGroups = new LinkedHashMap<>();
        // key -> list of ScheduledLesson objects for that key
        Map<String, List<ScheduledLesson>> lessonsByKey = new HashMap<>();

        for (int week = 0; week < maxWeeks; week++) {
            final int finalWeek = week;
            // collect active lessons this week
            List<ScheduledLesson> active = timetable.getScheduledLessonList().stream()
                    .filter(l -> {
                        String weeks = l.getWeeksBinaryString();
                        return finalWeek < weeks.length() && weeks.charAt(finalWeek) == '1';
                    })
                    .collect(Collectors.toList());

            // create a key to represent all lessons
            List<String> ids = active.stream()
                    .map(ScheduledLesson::getClassId)
                    .sorted()
                    .collect(Collectors.toList());

            String key = String.join("|", ids); // key creation

            if (weekGroups.containsKey(key)) {
                weekGroups.get(key).add(week + 1);
            } else {
                weekGroups.put(key, new ArrayList<>(List.of(week + 1)));
                lessonsByKey.put(key, active);
            }
        }

        // TabPane: one tab per unique week pattern
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        for (Map.Entry<String, List<Integer>> entry : weekGroups.entrySet()) {
            String key = entry.getKey();
            List<Integer> weeks = entry.getValue();

            List<ScheduledLesson> lessonsForPattern = lessonsByKey.get(key);
            if (lessonsForPattern == null) continue;

            GridPane grid = buildGridForLessons(
                    lessonsForPattern,
                    data,
                    minutesPerSlot,
                    minSlot,
                    visibleSlots,
                    TIME_COL_WIDTH,
                    DAY_SUBCOL_WIDTH,
                    ROW_HEIGHT
            );

            // Tab title shows the weeks that share this timetable
            String tabTitle = "Weeks " + weeks.toString();
            Tab tab = new Tab(tabTitle, new ScrollPane(grid));
            tabPane.getTabs().add(tab);
        }

        Scene scene = new Scene(tabPane, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Build the GridPane for a specific set of lessons
     */
    private GridPane buildGridForLessons(
            List<ScheduledLesson> lessons,
            InMemoryRepository data,
            int minutesPerSlot,
            int minSlotOffset,
            int visibleSlots,
            double timeColWidth,
            double daySubColWidth,
            double rowHeight
    ) {
        GridPane grid = new GridPane();
        grid.setGridLinesVisible(false);

        int days = data.getTimetableConfiguration().getNumDays();

        // Group lessons by day
        Map<Integer, List<ScheduledLesson>> lessonsByDay = new HashMap<>();
        for (ScheduledLesson l : lessons) {
            String mask = l.getDaysBinaryString();
            for (int d = 0; d < mask.length() && d < days; d++) {
                if (mask.charAt(d) == '1') {
                    lessonsByDay.computeIfAbsent(d, k -> new ArrayList<>()).add(l);
                }
            }
        }

        // For each day compute sub-column assignment (interval partitioning / greedy coloring)
        Map<ScheduledLesson, Integer> assignment = new IdentityHashMap<>();
        int[] subColsPerDay = new int[days];
        for (int d = 0; d < days; d++) {
            List<ScheduledLesson> dayList = lessonsByDay.getOrDefault(d, List.of())
                    .stream()
                    .sorted(Comparator.comparingInt(ScheduledLesson::getStartSlot))
                    .collect(Collectors.toList());

            List<Integer> lastEnd = new ArrayList<>(); // end slot for each subcol
            for (ScheduledLesson l : dayList) {
                int start = l.getStartSlot();
                int end = start + l.getLength();
                int found = -1;
                for (int sc = 0; sc < lastEnd.size(); sc++) {
                    if (start >= lastEnd.get(sc)) {
                        found = sc;
                        lastEnd.set(sc, end);
                        break;
                    }
                }
                if (found == -1) {
                    found = lastEnd.size();
                    lastEnd.add(end);
                }
                assignment.put(l, found);
            }
            subColsPerDay[d] = Math.max(1, lastEnd.size());
        }

        // Build column constraints: col 0 = time, then sum(subColsPerDay) subcolumns
        ColumnConstraints timeCol = new ColumnConstraints();
        timeCol.setMinWidth(timeColWidth);
        timeCol.setPrefWidth(timeColWidth);
        timeCol.setHgrow(Priority.NEVER);
        grid.getColumnConstraints().add(timeCol);

        int totalSubCols = Arrays.stream(subColsPerDay).sum();
        for (int i = 0; i < totalSubCols; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setMinWidth(daySubColWidth);
            cc.setPrefWidth(daySubColWidth);
            cc.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(cc);
        }

        // Number of Rows = header + visibleSlots
        RowConstraints headerRow = new RowConstraints();
        headerRow.setMinHeight(rowHeight);
        headerRow.setPrefHeight(rowHeight);
        headerRow.setMaxHeight(rowHeight);
        grid.getRowConstraints().add(headerRow);
        for (int r = 0; r < visibleSlots; r++) {
            RowConstraints rc = new RowConstraints();
            rc.setMinHeight(rowHeight);
            rc.setPrefHeight(rowHeight);
            rc.setMaxHeight(rowHeight);
            grid.getRowConstraints().add(rc);
        }

        // Header: one label per day spanning its sub-columns in width
        int colCursor = 1;
        for (int d = 0; d < days; d++) {
            int span = subColsPerDay[d];
            Label dayLabel = new Label(DAYS_OF_WEEK.get(d));
            dayLabel.setAlignment(Pos.CENTER);
            dayLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            dayLabel.setStyle(
                    "-fx-font-weight: bold;" +
                    "-fx-border-color: black;" +
                    "-fx-border-width: 1;" +
                    "-fx-background-color: #f0f0f0;" // Very light gray
            );

            grid.add(dayLabel, colCursor, 0, span, 1);
            GridPane.setHgrow(dayLabel, Priority.ALWAYS);
            colCursor += span;
        }

        // Time column + empty cells (to show a border in each cell)
        for (int s = 0; s < visibleSlots; s++) {
            int globalSlot = minSlotOffset + s;
            int startMin = globalSlot * minutesPerSlot;
            int endMin = (globalSlot + 1) * minutesPerSlot;
            int startHour = startMin / 60;
            int endHour = endMin / 60;

            Label timeLabel = new Label(String.format("%02d:%02d - %02d:%02d", startHour, startMin % 60, endHour, endMin % 60));
            timeLabel.setAlignment(Pos.CENTER);
            timeLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            timeLabel.setStyle("-fx-border-color: black; -fx-border-width: 1; -fx-background-color: #fafafa;");
            grid.add(timeLabel, 0, s + 1);

            // add empty panes for each sub-column so borders appear
            for (int c = 1; c <= totalSubCols; c++) {
                Pane cell = new Pane();
                cell.setStyle("-fx-border-color: lightgray; -fx-border-width: 1;");
                grid.add(cell, c, s + 1);
            }
        }

        // Place lessons into assigned sub-column
        for (ScheduledLesson l : lessons) {
            String mask = l.getDaysBinaryString();
            if (mask == null) continue;
            for (int d = 0; d < Math.min(mask.length(), days); d++) {
                if (mask.charAt(d) != '1') continue;

                int subColIndex = assignment.getOrDefault(l, 0);
                // compute column base for day d
                int dayColStart = 1; // after time column
                for (int dd = 0; dd < d; dd++) dayColStart += subColsPerDay[dd];

                int targetCol = dayColStart + subColIndex;

                int startSlot = l.getStartSlot();
                int duration = l.getLength();

                // clip to visible window
                if (startSlot + duration <= minSlotOffset) continue; // ends before visible window
                if (startSlot >= minSlotOffset + visibleSlots) continue; // starts after visible window

                int relativeStart = Math.max(0, startSlot - minSlotOffset);
                int visibleSpan = Math.min(duration, (minSlotOffset + visibleSlots) - startSlot);

                List<String> teacherList = new ArrayList<>();
                l.getTeachers().forEach((t) -> teacherList.add(t.getName()));

                Label idLabel = new Label(l.getClassId());
                Label progLabel = new Label("[" + data.getProgramName() + "]");
                Label teachersLabel = teacherList.isEmpty() ? null : new Label("[(" + String.join("); ", teacherList) + ")]");
                Label roomLabel = l.getRoomId() != null ? new Label(l.getRoomId()) : null;

                VBox box = new VBox(2);
                box.getChildren().add(idLabel);
                box.getChildren().add(progLabel);
                if(teachersLabel != null) box.getChildren().add(teachersLabel);
                if(roomLabel != null) box.getChildren().add(roomLabel);

                box.setAlignment(Pos.CENTER);
                box.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                box.setStyle("-fx-background-color: lightblue; -fx-border-color: darkblue; -fx-border-width: 1;");
                GridPane.setHgrow(box, Priority.ALWAYS);
                GridPane.setVgrow(box, Priority.ALWAYS);

                grid.add(box, targetCol, relativeStart + 1, 1, visibleSpan);
            }
        }

        // set preferred size so ScrollPane will show scrollbars properly
        double prefWidth = timeColWidth + totalSubCols * daySubColWidth;
        double prefHeight = (visibleSlots + 1) * rowHeight;
        grid.setPrefSize(prefWidth, prefHeight);
        grid.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        return grid;
    }

    @Override
    public void showExceptionMessage(Exception e) {
        windowManager.getExceptionMessage(e).show();
    }

    @Override
    public void setPrimaryWindow(Window primaryWindow) {
        this.primaryWindow = primaryWindow;

        primaryWindow.setWidth(DEFAULT_WIDTH);
        primaryWindow.setHeight(DEFAULT_HEIGHT);
    }

    private void clearTableView() {
        tableView.getColumns().clear();
        tableView.getItems().clear();
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

        windowManager = new WindowManager(primaryWindow);
        generalConfiguration = ConfigurationManager.loadConfig();
        progressBarManager = new ProgressBarManager(progressContainer);

        if(generalConfiguration.getShowTutorial()) {
            showTutorialMenuEvent();
            generalConfiguration.setTutorialShown();
        }
    }

    @Override
    public void cleanup() {
        // Only update the config file if the configuration was changed
        if(generalConfiguration.getUpdateConfigFile()) {
            ConfigurationManager.saveConfig(generalConfiguration);
        }

        progressBarManager.stopAndClearTimelines();
    }
}
