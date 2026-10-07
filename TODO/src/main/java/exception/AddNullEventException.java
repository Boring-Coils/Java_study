package exception;

/**
 * @author Bored_Coils
 */
public class AddNullEventException extends RuntimeException {
    public AddNullEventException(String message) {
        super(message);
    }

    public AddNullEventException(String message, Throwable cause) {
        super(message, cause);
    }
}
