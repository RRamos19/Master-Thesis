package thesis.model.parser;

public interface InputFileReader<T> {
    T readFile(String filePath);
}
