package learnMongoDb.learnSpringMongoDb.error;

public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException() {
        super("Too many login attempts. Please try again later.");
    }

    public RateLimitExceededException(String message) {
        super(message);
    }
}