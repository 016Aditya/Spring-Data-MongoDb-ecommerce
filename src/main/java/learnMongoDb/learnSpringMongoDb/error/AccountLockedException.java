package learnMongoDb.learnSpringMongoDb.error;

public class AccountLockedException extends RuntimeException {
    public AccountLockedException(String message) {
        super(message);
    }
}