package devtools.exception;

public abstract class DevToolsException extends Exception {

    public DevToolsException(String message) {
        super(message);
    }

    public DevToolsException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
