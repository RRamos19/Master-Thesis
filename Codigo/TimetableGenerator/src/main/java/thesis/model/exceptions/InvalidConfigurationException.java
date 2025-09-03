package thesis.model.exceptions;

/**
 * Its throw indicates invalid values in the configuration of a problem. The message should then clarify
 * where was the problem and why it ocurred
 */
public class InvalidConfigurationException extends Exception {
    public InvalidConfigurationException(String message) {
        super(message);
    }
}
