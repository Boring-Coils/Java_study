package exception;

/**
 * FileManager读写文件时出现意外
 * @author Bored_Coils
 */
public class StorageException extends RuntimeException {

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}