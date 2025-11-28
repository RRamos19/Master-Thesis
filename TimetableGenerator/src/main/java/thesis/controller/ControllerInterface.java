package thesis.controller;

import javafx.scene.control.TreeItem;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Window;
import thesis.model.ModelInterface;
import thesis.model.domain.InMemoryRepository;
import thesis.model.domain.components.Timetable;
import thesis.view.ViewInterface;

public interface ControllerInterface {
    // Setters
    void setModel(ModelInterface model);
    void setView(ViewInterface view);
    void setPrimaryWindow(Window primaryWindow);

    // Button events
    void connectEvent();
    void importDataITCEvent();
    void configMenuEvent();
    void exportDataITCEvent();
    void exportDataCSVEvent();
    void exportSolutionITCEvent();
    void exportSolutionPNGEvent();
    void exportSolutionPDFEvent();
    void dragOverEvent(DragEvent event);
    void dragEnteredEvent(DragEvent event);
    void dragExitedEvent(DragEvent event);
    void dragDroppedEvent(DragEvent event);
    void generateSolutionEvent();
    void removeTableInstanceEvent();
    void reoptimizeSolutionEvent();
    void showInstructionsMenuEvent();
    void changeProgramChoiceEvent(String newValue);
    void tableViewMouseClickedEvent(MouseEvent event);
    void progressContainterResizeEvent();
    void removeProgramEvent();

    // Show results
    void updateStoredPrograms();
    void updateTableView();
    void updateTableView(TreeItem<Controller.TableType> item);
    void showTimetable(InMemoryRepository data, Timetable timetable);

    // Show alerts to user
    void showInformationAlert(String message);
    void showErrorAlert(String message);
    boolean showConfirmationAlert(String message);
    void showExceptionMessage(Throwable e);

    // Initialize and cleanup the controller
    void initialize();
    void cleanup();
}
