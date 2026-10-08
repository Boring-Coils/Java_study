package exception;

/**
 * 向TODOList里面添加事项时传入了空值
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
