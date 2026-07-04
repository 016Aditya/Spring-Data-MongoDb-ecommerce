package learnMongoDb.learnSpringMongoDb.service;

import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final Supplier<Bucket> loginBucketSupplier;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Value("${security.login.rate-limit.use-ip-and-email:true}")
    private boolean useIpAndEmail;

    public boolean tryConsume(String ip, String email) {
        String key = generateKey(ip, email);
        Bucket bucket = buckets.computeIfAbsent(
                key,
                k -> loginBucketSupplier.get()
        );
        return bucket.tryConsume(1);
    }

    public long getRemainingTokens(String ip, String email) {
        String key = generateKey(ip, email);
        Bucket bucket = buckets.computeIfAbsent(
                key,
                k -> loginBucketSupplier.get()
        );
        return bucket.getAvailableTokens();
    }

    private String generateKey(String ip, String email) {
        if (useIpAndEmail && email != null && !email.isBlank()) {
            return ip + ":" + email.toLowerCase();
        }
        return ip;
    }

    public void reset(String ip, String email) {
        buckets.remove(generateKey(ip, email));
    }

    public void clearAll() {
        buckets.clear();
    }

    public int getLimit() {
        return 5;
    }

    public Duration getWindow() {
        return Duration.ofMinutes(1);
    }
}