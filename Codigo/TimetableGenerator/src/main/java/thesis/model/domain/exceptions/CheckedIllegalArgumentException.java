package thesis.model.domain.exceptions;

/**
 * Basically the same as IllegalArgumentException but this one is checked, which makes it easier to catch
 */
public class CheckedIllegalArgumentException extends Exception {
    public CheckedIllegalArgumentException(String message) {
        super(message);
    }
}
