package learnMongoDb.learnSpringMongoDb.service;

import learnMongoDb.learnSpringMongoDb.dto.turnstile.TurnstileVerifyRequest;
import learnMongoDb.learnSpringMongoDb.dto.turnstile.TurnstileVerifyResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class TurnstileService {

    private final RestClient restClient;

    @Value("${turnstile.secret-key}")
    private String secretKey;

    @Value("${turnstile.verify-url}")
    private String verifyUrl;

    public TurnstileService(RestClient turnstileRestClient) {
        this.restClient = turnstileRestClient;
    }

    public boolean verify(String token, String remoteIp) {
        if (token == null || token.isBlank()) {
            return false;
        }

        try {
            TurnstileVerifyRequest request = new TurnstileVerifyRequest(secretKey, token, remoteIp);

            TurnstileVerifyResponse response = restClient.post()
                    .uri(verifyUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(TurnstileVerifyResponse.class);

            return response != null && response.success();

        } catch (Exception e) {
            // Log the error. Fail closed by returning false if Cloudflare is unreachable.
            return false;
        }
    }
}