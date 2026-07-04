package learnMongoDb.learnSpringMongoDb.dto.turnstile;

public record TurnstileVerifyRequest(
        String secret,
        String response,
        String remoteip
) {}