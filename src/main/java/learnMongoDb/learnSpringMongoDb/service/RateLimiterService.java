package learnMongoDb.learnSpringMongoDb.service;

import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
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

    public boolean tryConsume(String key) {

        Bucket bucket = buckets.computeIfAbsent(
                key,
                k -> loginBucketSupplier.get()
        );

        return bucket.tryConsume(1);
    }

    public long getRemainingTokens(String key) {

        Bucket bucket = buckets.computeIfAbsent(
                key,
                k -> loginBucketSupplier.get()
        );

        return bucket.getAvailableTokens();
    }

    public void reset(String key) {
        buckets.remove(key);
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