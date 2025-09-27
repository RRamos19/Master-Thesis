package thesis.view;

import javafx.stage.Window;
import thesis.controller.ControllerInterface;

public interface ViewInterface {
    void setController(ControllerInterface controller);
    void showExceptionMessage(Exception e);
    void setPrimaryWindow(Window primaryWindow);

    void showInformationAlert(String message);
    void showErrorAlert(String message);
    boolean showConfirmationAlert(String message);

    void updateTableView();

    // Runs at the end of the graphical application
    void cleanup();
}
