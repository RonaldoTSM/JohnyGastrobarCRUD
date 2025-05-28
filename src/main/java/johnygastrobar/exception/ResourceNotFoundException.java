package johnygastrobar.exception;

public class ResourceNotFoundException extends ServiceException { // Herda de ServiceException
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}