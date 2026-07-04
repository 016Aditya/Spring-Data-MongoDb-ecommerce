package learnMongoDb.learnSpringMongoDb.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class TurnstileConfig {

    @Value("${turnstile.timeout-seconds:3}")
    private int timeoutSeconds;

    @Bean
    public RestClient turnstileRestClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutSeconds * 1000);
        factory.setReadTimeout(timeoutSeconds * 1000);

        return RestClient.builder()
                .requestFactory(factory)
                .build();
    }
}