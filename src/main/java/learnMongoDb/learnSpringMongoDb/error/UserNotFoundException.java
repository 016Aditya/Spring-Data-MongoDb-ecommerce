package learnMongoDb.learnSpringMongoDb.error;

/**
 * Thrown when a user document cannot be located by ID.
 * Mapped to HTTP 404 Not Found by GlobalExceptionHandler.
 */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
