package learnMongoDb.learnSpringMongoDb.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JwtAuthFilter
 *
 * Runs once per request. Extracts the Bearer token from the Authorization
 * header, validates it, and populates the Spring SecurityContext with a
 * CustomUserDetails principal.
 *
 * If the token is missing or invalid, the filter passes the request through
 * without setting a principal. Spring Security's authorization rules then
 * reject the unauthenticated request with 401.
 *
 * IMPORTANT: The userId is read exclusively from the validated JWT claims
 * (the "sub" field). No userId from the request body is ever trusted.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // No Authorization header or not a Bearer token — skip, let Spring Security handle auth
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

                String userId = claims.getSubject();                   // trusted userId from JWT
                String role   = claims.get("role", String.class);     // trusted role from JWT

                CustomUserDetails principal = new CustomUserDetails(userId, role);

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
            // Invalid / expired / malformed token — clear context, let Spring Security return 401
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
