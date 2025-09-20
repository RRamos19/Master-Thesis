package thesis.model.parser;

import thesis.model.exceptions.InvalidConfigurationException;
import thesis.model.exceptions.ParsingException;

import java.io.File;

public interface InputFileReader {
    XmlResult readXmlFile(File file) throws ParsingException, InvalidConfigurationException;
}
