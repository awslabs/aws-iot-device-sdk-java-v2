package software.amazon.awssdk.eventstreamrpc;

public class InvalidServiceConfigurationException extends RuntimeException {
    public InvalidServiceConfigurationException(String msg) {
        super(msg);
    }

    public InvalidServiceConfigurationException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public InvalidServiceConfigurationException(Throwable cause) {
        super(cause);
    }
}
