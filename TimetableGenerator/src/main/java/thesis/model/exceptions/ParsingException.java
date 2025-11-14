package thesis.model.exceptions;

import javax.xml.stream.Location;

public class ParsingException extends Exception {
    public ParsingException(String message) {
        super("Parsing Error - " + message);
    }

    public ParsingException(Location parserLocation, String message) {
        this("In line " + parserLocation.getLineNumber() + " column " + parserLocation.getColumnNumber() + ": " + message);
    }

    public ParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
