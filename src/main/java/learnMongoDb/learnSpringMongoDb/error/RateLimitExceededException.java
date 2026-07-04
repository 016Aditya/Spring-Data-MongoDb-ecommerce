package learnMongoDb.learnSpringMongoDb.error;

public class RateLimitExceededException extends RuntimeException {

    private final int remainingSeconds;

    // Constructor for when you want to pass the remaining time (like in UserController)
    public RateLimitExceededException(String message, int remainingSeconds) {
        super(message);
        this.remainingSeconds = remainingSeconds;
    }

    // Overloaded constructor in case you throw it elsewhere without a time limit
    public RateLimitExceededException(String message) {
        super(message);
        this.remainingSeconds = 0;
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
    }
}