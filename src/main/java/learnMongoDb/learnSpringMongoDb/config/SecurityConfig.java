package learnMongoDb.learnSpringMongoDb.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import learnMongoDb.learnSpringMongoDb.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Map;

/**
 * SecurityConfig
 *
 * JWT-based stateless security configuration.
 *
 * HTTP status contract:
 *   401 Unauthorized  — no token, or token is invalid/expired.
 *   403 Forbidden     — valid token, but user’s role lacks permission.
 *
 * Public endpoints (no token required):
 *   POST /api/users/register
 *   POST /api/users/login
 *   POST /api/users/forgot-password
 *   POST /api/users/verify-identity
 *   POST /api/users/reset-password
 *   GET  /api/products/**          (guest browsing)
 *   GET  /api/reviews/**           (guest reading)
 *   POST /api/reviews/search       (guest filtering)
 *
 * All other endpoints, including ALL /api/cart/** routes, require a valid
 * Bearer JWT. Cart is server-side state tied to a userId from the JWT;
 * allowing unauthenticated GET would cause a NullPointerException when
 * the controller calls principal.getUserId() on a null principal.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    /**
     * AuthenticationEntryPoint — invoked when a protected endpoint is reached
     * with NO authentication principal (token entirely missing).
     * Returns 401 + JSON.
     */
    @Bean
    public AuthenticationEntryPoint unauthorizedEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(401);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            new ObjectMapper().writeValue(
                    response.getWriter(),
                    Map.of("error", "Unauthorized", "message", "Authentication required. Please log in."));
        };
    }

    /**
     * AccessDeniedHandler — invoked when an authenticated user’s role does not
     * meet the endpoint’s authorization requirement.
     * Returns 403 + JSON.
     */
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(403);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            new ObjectMapper().writeValue(
                    response.getWriter(),
                    Map.of("error", "Forbidden", "message", "You do not have permission to access this resource."));
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(unauthorizedEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler()))
                .authorizeHttpRequests(auth -> auth

                        // Auth endpoints — no token available yet
                        .requestMatchers(HttpMethod.POST, "/api/users/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users/forgot-password").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users/verify-identity").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users/reset-password").permitAll()

                        // Product catalogue — public for guest browsing
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()

                        // Reviews — guests can read; authenticated users can write
                        .requestMatchers(HttpMethod.GET,  "/api/reviews/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/reviews/search").permitAll()

                        // Everything else (including ALL /api/cart/**) requires a valid JWT
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}