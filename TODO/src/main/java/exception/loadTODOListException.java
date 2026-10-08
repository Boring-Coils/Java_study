package exception;

public class loadTODOListException extends RuntimeException {
    public loadTODOListException(String message) {
        super(message);
    }
    public loadTODOListException(String message, Throwable cause) {
        super(message, cause);
    }
}
