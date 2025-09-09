package thesis.model.exceptions;

/**
 * The same as IllegalStateException but this one is checked, which makes it easier to catch
 */
public class CheckedIllegalState extends Exception {
    public CheckedIllegalState(String message) {
        super(message);
    }
}
