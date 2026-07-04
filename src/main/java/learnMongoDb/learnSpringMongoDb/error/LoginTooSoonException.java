package learnMongoDb.learnSpringMongoDb.error;

public class LoginTooSoonException extends RuntimeException {
    private final long retryAfter;

    public LoginTooSoonException(long retryAfter) {
        super("Please wait before trying again.");
        this.retryAfter = retryAfter;
    }

    public long getRetryAfter() {
        return retryAfter;
    }
}