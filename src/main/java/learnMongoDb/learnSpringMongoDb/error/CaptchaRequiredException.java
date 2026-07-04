package learnMongoDb.learnSpringMongoDb.error;

public class CaptchaRequiredException extends RuntimeException {
    public CaptchaRequiredException(String message) {
        super(message);
    }
}