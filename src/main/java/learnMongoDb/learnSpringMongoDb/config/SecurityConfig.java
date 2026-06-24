package learnMongoDb.learnSpringMongoDb.config;

import learnMongoDb.learnSpringMongoDb.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SecurityConfig
 *
 * JWT-based stateless security configuration.
 *
 * Public endpoints (no token required):
 * POST /api/users/register
 * POST /api/users/login
 * POST /api/users/forgot-password
 * POST /api/users/verify-identity
 * POST /api/users/reset-password
 * GET  /api/products/** (guest browsing)
 * GET  /api/reviews/** (guest reading reviews)
 * POST /api/reviews/search    (guest filtering reviews)
 *
 * All other endpoints require a valid Bearer JWT in the Authorization header.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Tell Spring Security to honour the CorsFilter bean defined in CorsConfig.
                .cors(Customizer.withDefaults())

                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        // Auth endpoints — no token available yet
                        .requestMatchers(HttpMethod.POST, "/api/users/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users/login").permitAll()

                        // Forgot-password flow — unauthenticated by definition
                        .requestMatchers(HttpMethod.POST, "/api/users/forgot-password").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users/verify-identity").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users/reset-password").permitAll()

                        // Product catalogue is public for guest browsing
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()

                        // Public Review endpoints for guests
                        .requestMatchers(HttpMethod.GET, "/api/reviews/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/reviews/search").permitAll()

                        // --- FIX: Allow guest access to the Cart ---
                        // Note: We use the generic path without HttpMethod to allow GET, POST, PUT, DELETE
                        .requestMatchers("/api/cart/**").permitAll()

                        // All other endpoints (e.g., POST /api/reviews, PUT, DELETE) require a valid JWT
                        .anyRequest().authenticated()
                )

                // Register JWT filter before the default username/password filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}