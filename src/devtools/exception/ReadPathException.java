package devtools.exception;

public class ReadPathException extends DevToolsException {

    public ReadPathException(String message) {
        super(message);
    }

    public ReadPathException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
