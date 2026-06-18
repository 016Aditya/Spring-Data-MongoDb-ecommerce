package learnMongoDb.learnSpringMongoDb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SecurityConfig
 *
 * ROOT CAUSE OF LOGIN FAILURE:
 * This entire file was commented out. Spring Boot's DEFAULT security then
 * applied automatically:
 *   - HTTP Basic auth required on every request
 *   - CSRF enabled  ->  every POST/PUT/DELETE blocked with 403 Forbidden
 *   - Stateful sessions
 *
 * Result: POST /api/users/login returned 403 before the controller ran.
 *
 * Fix:
 *   1. CSRF disabled  (REST API uses tokens, not browser-session cookies)
 *   2. Stateless session management
 *   3. Register + Login + Forgot-Password endpoints publicly accessible
 *   4. Product GET endpoints publicly accessible (guest browsing)
 *   5. All other endpoints require an authenticated user
 *      (replace .authenticated() with JWT filter when token issuance added)
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)

            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth

                // Auth endpoints - no token available yet
                .requestMatchers(HttpMethod.POST, "/api/users/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/users/login").permitAll()

                // Forgot-password flow - unauthenticated by definition
                .requestMatchers(HttpMethod.POST, "/api/users/forgot-password").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/users/verify-identity").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/users/reset-password").permitAll()

                // Product catalogue is public for guest browsing
                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()

                // All other endpoints require authentication
                // TODO: swap for JWT filter when token issuance is implemented
                .anyRequest().authenticated()
            );

        return http.build();
    }
}
