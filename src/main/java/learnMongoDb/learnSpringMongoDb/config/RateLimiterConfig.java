package learnMongoDb.learnSpringMongoDb.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.function.Supplier;

@Configuration
public class RateLimiterConfig {

    public static final int LOGIN_REQUEST_LIMIT = 5;

    public static final Duration REFILL_DURATION = Duration.ofMinutes(1);

    @Bean
    public Supplier<Bucket> loginBucketSupplier() {

        Bandwidth limit = Bandwidth.classic(
                LOGIN_REQUEST_LIMIT,
                Refill.greedy(LOGIN_REQUEST_LIMIT, REFILL_DURATION)
        );

        return () -> Bucket.builder()
                .addLimit(limit)
                .build();
    }
}