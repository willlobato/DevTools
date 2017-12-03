package devtools.liberty.exception;

public class LibertyAccessException extends Exception {

    public LibertyAccessException(String message) {
        super(message);
    }

    public LibertyAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
