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

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);

        try {
            Claims claims = jwtUtil.validateAndExtractClaims(token);

            if (SecurityContextHolder.getContext().getAuthentication() == null) {

                String userId   = claims.getSubject();
                String email    = claims.get("email",    String.class);  // FIX: was missing
                String role     = claims.get("role",     String.class);
                String fullName = claims.get("fullName", String.class);

                if (email    == null) email    = "";
                if (fullName == null) fullName = "";

                // Constructor signature: CustomUserDetails(userId, email, role, fullName)
                CustomUserDetails principal = new CustomUserDetails(userId, email, role, fullName);

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                principal.getAuthorities());

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

        } catch (JwtException | IllegalArgumentException e) {
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(
                    response.getWriter(),
                    Map.of(
                            "error",   "Unauthorized",
                            "message", "Your session has expired or the token is invalid. Please log in again."
                    )
            );
            return;
        }

        filterChain.doFilter(request, response);
    }
}