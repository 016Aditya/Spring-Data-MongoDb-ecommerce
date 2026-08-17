package learnMongoDb.learnSpringMongoDb.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JwtUtil
 *
 * Handles JWT creation and validation.
 *
 * Token claims:
 *   sub      — MongoDB userId
 *   email    — user's email address
 *   role     — user's role (USER / ADMIN)
 *   fullName — firstName + " " + lastName
 */
@Component
public class JwtUtil {

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expirationMs) {

        this.signingKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );

        this.expirationMs = expirationMs;
    }

    // ── Token generation ─────────────────────────────────────────────────────

    /**
     * Creates a signed JWT embedding userId, email, role, and fullName.
     *
     * @param userId   MongoDB _id of the authenticated user
     * @param email    user's email
     * @param role     user's role ("USER" or "ADMIN")
     * @param fullName firstName + " " + lastName
     * @return compact signed JWT string
     */
    public String generateToken(
            String userId,
            String email,
            String role,
            String fullName
    ) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
                .subject(userId)
                .claim("email", email)
                .claim("role", role)
                .claim("fullName", fullName)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationMs))
                .signWith(signingKey)
                .compact();
    }

    // ── Token validation ─────────────────────────────────────────────────────

    /**
     * Validates the token signature and expiry.
     *
     * @param token compact JWT string without "Bearer " prefix
     * @return parsed JWT claims
     * @throws JwtException if token is malformed, expired,
     *                      or the signature is invalid
     */
    public Claims validateAndExtractClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // ── Claim extractors ─────────────────────────────────────────────────────

    public String extractUserId(String token) {
        return validateAndExtractClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return validateAndExtractClaims(token)
                .get("role", String.class);
    }

    public String extractFullName(String token) {
        return validateAndExtractClaims(token)
                .get("fullName", String.class);
    }

    // ── Safe validation ──────────────────────────────────────────────────────

    /**
     * Returns false instead of throwing if the token is invalid.
     */
    public boolean isValid(String token) {
        try {
            validateAndExtractClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}