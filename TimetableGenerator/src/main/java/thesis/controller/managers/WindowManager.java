package thesis.controller.managers;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import thesis.controller.ControllerInterface;
import thesis.model.domain.InMemoryRepository;
import thesis.model.domain.components.ScheduledLesson;
import thesis.model.domain.components.Timetable;
import thesis.view.utils.AppIcons;
import thesis.controller.managers.components.GeneralConfiguration;
import thesis.controller.managers.windows.ConfigWindow;
import thesis.controller.managers.windows.ExceptionMessageWindow;
import thesis.view.utils.Defaults;

import java.util.*;
import java.util.stream.Collectors;

public class WindowManager {
    private static final List<String> DAYS_OF_WEEK = List.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");
    private final Window primaryWindow;
    private final ControllerInterface controller;
    private final GeneralConfiguration generalConfiguration;
    private ExceptionMessageWindow exceptionMessageWindow;
    private Stage instructionsWindow;
    private ConfigWindow configWindow;
    private final Map<Timetable, Stage> timetableWindowCache = new HashMap<>();

    public WindowManager(Window primaryWindow, ControllerInterface controller, GeneralConfiguration generalConfiguration) {
        this.primaryWindow = primaryWindow;
        this.controller = controller;
        this.generalConfiguration = generalConfiguration;
    }

    public Alert getExceptionMessage(Throwable e) {
        if(exceptionMessageWindow == null) {
            exceptionMessageWindow = new ExceptionMessageWindow();
        }

        exceptionMessageWindow.setMessages(e);

        return exceptionMessageWindow.getExceptionAlert();
    }

    public Stage getInstructionsWindow() {
        if(instructionsWindow == null) {
            instructionsWindow = new Stage();
            instructionsWindow.getIcons().addAll(AppIcons.getAppIcons());
            instructionsWindow.initStyle(StageStyle.DECORATED);
            instructionsWindow.initOwner(primaryWindow);
            instructionsWindow.initModality(Modality.WINDOW_MODAL);
            instructionsWindow.setTitle("System Instructions");

            VBox box = new VBox(5);
            box.setPadding(new Insets(10));
            box.setAlignment(Pos.CENTER_LEFT);

            Label title = new Label("System Usage Instructions");
            title.setMaxWidth(Double.MAX_VALUE);
            title.setAlignment(Pos.TOP_CENTER);

            box.getChildren().add(title);

            List<String> labels = List.of(
                "1. For the system to function correctly, follow these steps:",
                "\tConnect to a PostgreSQL database",
                "\tEnter the database IP address and Port in the corresponding fields.",
                "\tEnter the username and password in the corresponding fields.",
                "\tPress the \"Connect\" button.",
                "\tIf the database has data it will then be displayed by the graphical interface.",
                "\tNote - Alternatively, you may import one or more files into the system instead of connecting to a database.",

                "2. Verify data availability",
                "\tOnce data is loaded, a program name will appear in the top-left corner of the interface.",
                "\tOn the right side of the program name an X button may be pressed to remove said data from the interface.",
                "\tBelow it, you will find selectable items. When clicked, these display the contents of the corresponding tables in the central area of the window.",

                "3. Generate timetables",
                "\tPress the button located at the bottom-right corner to start the generation process.",
                "\tA progress bar at the bottom of the window will indicate task progress.",
                "\tAfter completion, the item called \"Timetable\" may be selected to display all generated solutions for the chosen program.",

                "4. Task management",
                "\tThe application supports multiple generation tasks running in parallel.",
                "\tMultiple tasks can be executed at a time. Each one has a button to cancel said task.",

                "5. Exporting the data",
                "\tOn the top menu, the option \"File\" contains all the choices of exportation.",
                "\tUpon choosing one of the options, all the data of the chosen program will be exported in the chosen format.",
                "\tFor the exportation of solutions each one must be chosen before performing the exportation."
            );

            for(String line : labels) {
                Label textLabel = new Label(line);
                textLabel.setTextAlignment(TextAlignment.LEFT);
                textLabel.setWrapText(true);

                box.getChildren().add(textLabel);
            }

            // Close button
            Button closeBtn = new Button("Close");
            closeBtn.setOnAction(event -> instructionsWindow.close());
            HBox closeBTNBox = new HBox(closeBtn);
            closeBTNBox.setAlignment(Pos.CENTER_RIGHT);

            box.getChildren().add(closeBTNBox);

            instructionsWindow.setScene(new Scene(box));
            instructionsWindow.setAlwaysOnTop(true);
        }

        return instructionsWindow;
    }

    public Stage getConfigWindow(GeneralConfiguration generalConfiguration) {
        if(configWindow == null) {
            configWindow = new ConfigWindow(primaryWindow, generalConfiguration, controller);
        }

        configWindow.resetLabels();
        return configWindow.getConfigStage();
    }

    /**
     * Build the GridPane for a specific set of lessons
     */
    private GridPane buildGridForLessons(
            List<ScheduledLesson> lessons,
            InMemoryRepository data,
            int minutesPerSlot,
            int minSlot,
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
        for (ScheduledLesson lesson : lessons) {
            int startSlot = lesson.getStartSlot();
            int duration = lesson.getLength();

            // clip to visible window
            if ((startSlot + duration) <= minSlot) continue; // ends before visible window
            if (startSlot >= (minSlot + visibleSlots)) continue; // starts after visible window

            String mask = lesson.getDaysBinaryString();
            for (int day = 0; day < mask.length() && day < days; day++) {
                if (mask.charAt(day) == '1') {
                    lessonsByDay.computeIfAbsent(day, k -> new ArrayList<>()).add(lesson);
                }
            }
        }

        // For each day compute sub-column assignment
        Map<ScheduledLesson, Integer> assignment = new IdentityHashMap<>();
        int[] subColsPerDay = new int[days];
        for (int day = 0; day < days; day++) {
            List<ScheduledLesson> dayList = lessonsByDay.getOrDefault(day, List.of())
                    .stream()
                    .sorted(Comparator.comparingInt(ScheduledLesson::getStartSlot))
                    .collect(Collectors.toList());

            List<Integer> lastEnd = new ArrayList<>(); // end slot for each sub-column
            for (ScheduledLesson lesson : dayList) {
                int start = lesson.getStartSlot();
                int end = lesson.getEndSlot();
                int found = -1;
                for (int subCol = 0; subCol < lastEnd.size(); subCol++) {
                    if (start >= lastEnd.get(subCol)) {
                        found = subCol;
                        lastEnd.set(subCol, end);
                        break;
                    }
                }
                if (found == -1) {
                    found = lastEnd.size();
                    lastEnd.add(end);
                }
                assignment.put(lesson, found);
            }
            subColsPerDay[day] = Math.max(1, lastEnd.size());
        }

        // Build column constraints: col 0 = time, then sum(subColsPerDay) sub-columns
        ColumnConstraints timeCol = new ColumnConstraints();
        timeCol.setMinWidth(timeColWidth);
        timeCol.setPrefWidth(timeColWidth);
        timeCol.setHgrow(Priority.NEVER);
        grid.getColumnConstraints().add(timeCol);

        int totalSubCols = Arrays.stream(subColsPerDay).sum();

        // Header: one label per day spanning its sub-columns in width
        int colCursor = 1;
        for (int d = 0; d < days; d++) {
            int span = subColsPerDay[d];
            Label dayLabel = new Label(DAYS_OF_WEEK.get(d));
            dayLabel.setAlignment(Pos.CENTER);
            dayLabel.setMinWidth(dayLabel.getPrefWidth());
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
        for (int slot = 0; slot < visibleSlots; slot++) {
            int globalSlot = minSlot + slot;
            int startMin = globalSlot * minutesPerSlot;
            int endMin = (globalSlot + 1) * minutesPerSlot;
            int startHour = startMin / 60;
            int endHour = endMin / 60;

            Label timeLabel = new Label(String.format("%02d:%02d - %02d:%02d", startHour, startMin % 60, endHour, endMin % 60));
            timeLabel.setAlignment(Pos.CENTER);
            timeLabel.setMinHeight(rowHeight);
            timeLabel.setPrefHeight(rowHeight);
            timeLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            timeLabel.setStyle("-fx-border-color: black; -fx-border-width: 1; -fx-background-color: #fafafa;");
            grid.add(timeLabel, 0, slot + 1);

            // add empty panes for each sub-column so borders appear
            for (int c = 1; c <= totalSubCols; c++) {
                Pane cell = new Pane();
                cell.setMinHeight(rowHeight);
                cell.setPrefHeight(rowHeight);
                cell.setStyle("-fx-border-color: lightgray; -fx-border-width: 1;");
                GridPane.setVgrow(cell, Priority.NEVER);
                grid.add(cell, c, slot + 1);
            }
        }

        // Place lessons into assigned sub-column
        for (int day = 0; day < days; day++) {
            List<ScheduledLesson> dayLessons = lessonsByDay.get(day);
            if(dayLessons == null) continue;

            for (ScheduledLesson lesson : dayLessons) {
                int startSlot = lesson.getStartSlot();
                int duration = lesson.getLength();

                int subColIndex = assignment.get(lesson);
                // compute column base for day
                int dayColStart = 1; // after time column
                for (int dd = 0; dd < day; dd++) dayColStart += subColsPerDay[dd];

                int targetCol = dayColStart + subColIndex;

                // A 1 is added because of the header with days of week
                int relativeStart = Math.max(0, startSlot - minSlot) + 1;

                int visibleSpan = Math.min(duration, (minSlot + visibleSlots) - startSlot);

                List<String> teacherList = new ArrayList<>();
                lesson.getTeachers().forEach((t) -> teacherList.add(t.getName()));

                Label idLabel = new Label(lesson.getClassId());
                Label progLabel = new Label("[" + data.getProgramName() + "]");
                Label teachersLabel = teacherList.isEmpty() ? null : new Label("[(" + String.join(");\n ", teacherList) + ")]");
                Label roomLabel = lesson.getRoomId() != null ? new Label("[" + lesson.getRoomId() + "]") : null;

                VBox box = new VBox(2);
                ObservableList<Node> boxChildren = box.getChildren();
                boxChildren.add(idLabel);
                boxChildren.add(progLabel);

                if(teachersLabel != null) {
                    teachersLabel.setTextAlignment(TextAlignment.CENTER);
                    teachersLabel.setWrapText(true);
                    teachersLabel.setMaxWidth(box.getPrefWidth());
                    boxChildren.add(teachersLabel);
                }
                if(roomLabel != null) boxChildren.add(roomLabel);

                box.setAlignment(Pos.CENTER);
                box.setMinHeight(rowHeight*visibleSpan);
                box.setPrefHeight(rowHeight*visibleSpan);
                box.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                box.setStyle("-fx-background-color: lightblue; -fx-border-color: black; -fx-border-width: 1;");
                GridPane.setHgrow(box, Priority.ALWAYS);
                GridPane.setVgrow(box, Priority.NEVER);

                grid.add(box, targetCol, relativeStart, 1, visibleSpan);
            }
        }

        // set preferred size so ScrollPane shows scrollbars properly
        double prefWidth = timeColWidth + totalSubCols * daySubColWidth;
        grid.setPrefWidth(prefWidth);
        grid.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        return grid;
    }

    public void removeTimetableCache(Timetable timetable) {
        timetableWindowCache.remove(timetable);
    }

    public Stage getTimetableWindow(InMemoryRepository data, Timetable timetable) {
        Stage timetableWindow = timetableWindowCache.get(timetable);

        if(timetableWindow == null) {
            timetableWindow = new Stage();
            timetableWindow.getIcons().addAll(AppIcons.getAppIcons());
            timetableWindow.setTitle(timetable.getProgramName());

            if (primaryWindow != null) {
                timetableWindow.initOwner(primaryWindow);
                timetableWindow.initModality(Modality.WINDOW_MODAL);
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

                // Let the grid grow
                grid.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

                // Fit the ScrollPane to the size of the window
                ScrollPane scrollPane = new ScrollPane(grid);
                scrollPane.setFitToHeight(true);
                scrollPane.setFitToWidth(true);
                scrollPane.setPannable(true);

                // Tab title shows the weeks that share this timetable
                String tabTitle = "Weeks " + weeks.toString();
                Tab tab = new Tab(tabTitle, scrollPane);
                tabPane.getTabs().add(tab);
            }

            Scene scene = new Scene(tabPane, Defaults.DEFAULT_WIDTH, Defaults.DEFAULT_HEIGHT);
            timetableWindow.setScene(scene);

            timetableWindowCache.put(timetable, timetableWindow);
        }

        return timetableWindow;
    }
}
