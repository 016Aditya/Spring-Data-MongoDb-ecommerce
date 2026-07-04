package learnMongoDb.learnSpringMongoDb.error;

public class LoginTooSoonException extends RuntimeException {
    private final long remainingSeconds;

    public LoginTooSoonException(String message, long remainingSeconds) {
        super(message);
        this.remainingSeconds = remainingSeconds;
    }

    public long getRemainingSeconds() {
        return remainingSeconds;
    }
}