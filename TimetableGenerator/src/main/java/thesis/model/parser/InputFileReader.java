package thesis.model.parser;

import thesis.model.exceptions.CheckedIllegalArgumentException;
import thesis.model.exceptions.InvalidConfigurationException;
import thesis.model.exceptions.ParsingException;

import java.io.File;
import java.io.IOException;

public interface InputFileReader {
    XmlResult readFile(File file) throws ParsingException, InvalidConfigurationException, IOException, CheckedIllegalArgumentException;
}
