package thesis.model;

import thesis.controller.ControllerInterface;
import thesis.model.domain.exceptions.ParsingException;

import java.io.File;

public interface ModelInterface<T> {
    void setController(ControllerInterface controller);
    void importITCData(File file) throws ParsingException;
}
