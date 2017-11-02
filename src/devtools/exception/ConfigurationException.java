package devtools.exception;

public class ConfigurationException extends DevToolsException {

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
