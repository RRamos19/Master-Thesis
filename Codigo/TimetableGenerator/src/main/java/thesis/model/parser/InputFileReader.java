package thesis.model.parser;

import thesis.model.domain.DataRepository;
import thesis.model.domain.exceptions.ParsingException;

import java.io.File;

public interface InputFileReader<T> {
    T readFile(String filePath) throws ParsingException;
    T readFile(File file) throws ParsingException;
}
