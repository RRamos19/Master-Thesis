package thesis.view;

import thesis.controller.ControllerInterface;

public interface ViewInterface {
    void setController(ControllerInterface controller);
    void showExceptionMessage(Exception e);
}
