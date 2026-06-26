package learnMongoDb.learnSpringMongoDb.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/**
 * JwtAuthFilter
 *
 * Runs once per request. Extracts the Bearer token from the Authorization
 * header, validates it, and populates the Spring SecurityContext with a
 * CustomUserDetails principal.
 *
 * HTTP status contract:
 *   • No Authorization header          → pass through (Spring Security applies
 *                                        permitAll / authenticated rules downstream;
 *                                        AuthenticationEntryPoint returns 401 if needed)
 *   • Invalid / expired / bad token    → write HTTP 401 immediately and stop.
 *                                        Do NOT continue the filter chain.
 *                                        This prevents Spring Security's AccessDeniedHandler
 *                                        from returning a confusing 403 for an auth failure.
 *   • Valid token                       → populate SecurityContext, continue chain.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // No Authorization header or not a Bearer token — let the request through.
        // Spring Security will enforce permitAll / authenticated rules downstream.
        // If the endpoint is protected and there is no principal, the
        // AuthenticationEntryPoint will return 401.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7); // strip "Bearer "

        try {
            Claims claims = jwtUtil.validateAndExtractClaims(token);

            // Only set the authentication if the SecurityContext is currently empty
            // (avoids overwriting an already-authenticated context)
            if (SecurityContextHolder.getContext().getAuthentication() == null) {

                String userId   = claims.getSubject();                      // trusted userId from JWT
                String role     = claims.get("role",     String.class);     // trusted role from JWT
                String fullName = claims.get("fullName", String.class);     // display name from JWT

                // Fallback: if an old token was issued before fullName was added, use empty string
                if (fullName == null) fullName = "";

                CustomUserDetails principal = new CustomUserDetails(userId, role, fullName);

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                principal,
                                null,               // credentials — not needed post-validation
                                principal.getAuthorities());

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

        } catch (JwtException | IllegalArgumentException e) {
            // Token is present but invalid (expired, bad signature, malformed).
            // Write 401 directly and stop — do NOT continue the filter chain.
            //
            // OLD behaviour: clear context and call filterChain.doFilter() — Spring
            // Security then saw an unauthenticated request on a protected endpoint
            // and returned 403 (AccessDeniedHandler) instead of 401, because no
            // AuthenticationEntryPoint was configured. This confused the frontend
            // into thinking the user was authenticated but lacked a role, when the
            // real problem was simply an expired/rotated JWT.
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);  // 401
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(
                    response.getWriter(),
                    Map.of(
                            "error",   "Unauthorized",
                            "message", "Your session has expired or the token is invalid. Please log in again."
                    )
            );
            return; // stop — do not continue the filter chain
        }

        filterChain.doFilter(request, response);
    }
}