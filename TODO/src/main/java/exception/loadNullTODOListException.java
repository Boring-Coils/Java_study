package exception;

/**
 * 调用TODOList的loadTODOList()时传入了空值
 */
public class loadNullTODOListException extends RuntimeException {
    public loadNullTODOListException(String message) {
        super(message);
    }
    public loadNullTODOListException(String message, Throwable cause) {
        super(message, cause);
    }
}
