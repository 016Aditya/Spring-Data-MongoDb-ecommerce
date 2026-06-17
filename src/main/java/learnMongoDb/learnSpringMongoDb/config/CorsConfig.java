package learnMongoDb.learnSpringMongoDb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // ── Allowed origins (add production URL here when deploying) ────────────
        config.setAllowedOrigins(List.of(
                "http://localhost:5173",   // Vite dev server
                "http://localhost:4173"    // Vite preview
        ));

        // ── Methods ─────────────────────────────────────────────────────────────
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // ── Headers ─────────────────────────────────────────────────────────────
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-User-Id",
                "X-Requested-With",
                "Accept",
                "Origin"
        ));

        config.setExposedHeaders(List.of("Authorization"));

        // ── Credentials (cookies / Authorization header) ─────────────────────
        config.setAllowCredentials(true);

        // ── Cache preflight for 30 minutes — stops the OPTIONS storm ─────────
        config.setMaxAge(1800L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}