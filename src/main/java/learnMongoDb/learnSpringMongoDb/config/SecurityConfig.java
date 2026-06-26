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
 * HTTP status contract (enforced explicitly via entry point + denied handler):
 *   401 Unauthorized  — request has no token, or the token is invalid/expired.
 *                       The JwtAuthFilter writes 401 directly for bad tokens.
 *                       The AuthenticationEntryPoint writes 401 for missing tokens.
 *   403 Forbidden     — token is valid and authenticated, but the user's role
 *                       does not have permission for this resource.
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
 *   GET  /api/cart/**              (guest can view cart state client-side)
 *
 * All other endpoints require a valid Bearer JWT.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    /**
     * AuthenticationEntryPoint — invoked when a request reaches a protected
     * endpoint with NO authentication principal at all (token missing entirely).
     *
     * Returns 401 + JSON so the frontend can distinguish "not logged in" (401)
     * from "logged in but wrong role" (403).
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
     * AccessDeniedHandler — invoked when an authenticated user's role does not
     * meet the endpoint's authorization requirement.
     *
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
                // Honour the CorsFilter bean defined in CorsConfig
                .cors(Customizer.withDefaults())

                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Wire up our 401 / 403 handlers so the HTTP status contract is
                // clear and deterministic for the frontend.
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(unauthorizedEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler()))

                .authorizeHttpRequests(auth -> auth

                        // ── Auth endpoints — no token available yet ──────────────────────
                        .requestMatchers(HttpMethod.POST, "/api/users/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users/login").permitAll()

                        // Forgot-password flow — unauthenticated by definition
                        .requestMatchers(HttpMethod.POST, "/api/users/forgot-password").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users/verify-identity").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users/reset-password").permitAll()

                        // ── Product catalogue — public for guest browsing ─────────────────
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()

                        // ── Reviews — guests can read, authenticated users can write ──────
                        .requestMatchers(HttpMethod.GET,  "/api/reviews/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/reviews/search").permitAll()

                        // ── Cart — guests can READ cart state (GET only) ──────────────────
                        // POST / PUT / DELETE on /api/cart/** require authentication because
                        // they mutate server-side cart state tied to a userId from the JWT.
                        // The old rule (.requestMatchers("/api/cart/**").permitAll()) had no
                        // HttpMethod qualifier and permitted ALL mutations without a token.
                        .requestMatchers(HttpMethod.GET, "/api/cart/**").permitAll()

                        // ── Everything else requires a valid JWT ──────────────────────────
                        .anyRequest().authenticated()
                )

                // Register JWT filter before the default username/password filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}