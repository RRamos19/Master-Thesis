package thesis.model.parser;

import thesis.model.domain.exceptions.ParsingException;

public interface InputFileReader<T> {
    T readFile(String filePath) throws ParsingException;
}
