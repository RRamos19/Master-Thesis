package thesis.model.exceptions;

/**
 * The same as IllegalStateException but this one is checked, which makes it easier to catch
 */
public class CheckedIllegalStateException extends Exception {
    public CheckedIllegalStateException(String message) {
        super(message);
    }
}
