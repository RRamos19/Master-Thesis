package thesis.model.parser;

import thesis.model.domain.elements.exceptions.ParsingException;

import java.io.File;

public interface InputFileReader {
    XmlResult readXmlFile(File file) throws ParsingException;
}
