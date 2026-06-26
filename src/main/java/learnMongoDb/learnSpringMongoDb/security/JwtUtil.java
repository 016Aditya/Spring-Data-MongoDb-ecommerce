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
 *   sub      — MongoDB userId (the _id field of the User document)
 *   email    — user's email address
 *   role     — user's role (USER / ADMIN)
 *   fullName — firstName + " " + lastName (used by ReviewController to set userName)
 *
 * The secret is injected from application.properties (jwt.secret).
 * Minimum recommended secret length: 32 characters.
 */
@Component
public class JwtUtil {

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms:86400000}") long expirationMs) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    // ── Token generation ─────────────────────────────────────────────────────

    /**
     * Creates a signed JWT embedding userId, email, role, and fullName.
     *
     * @param userId    MongoDB _id of the authenticated user
     * @param email     user's email (informational, not used for auth decisions)
     * @param role      user's role string ("USER" or "ADMIN")
     * @param fullName  firstName + " " + lastName — stored as the "fullName" claim
     * @return compact signed JWT string
     */
    public String generateToken(String userId, String email, String role, String fullName) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(userId)              // sub = userId — this is what the filter trusts
                .claim("email",    email)
                .claim("role",     role)
                .claim("fullName", fullName)  // NEW — used to display real name on reviews
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationMs))
                .signWith(signingKey)
                .compact();
    }

    // ── Token validation ─────────────────────────────────────────────────────

    /**
     * Validates the token signature and expiry.
     * Returns the parsed Claims on success, or throws JwtException / IllegalArgumentException.
     *
     * @param token compact JWT string (without "Bearer " prefix)
     * @return parsed Claims
     * @throws JwtException if the token is malformed, expired, or the signature is invalid
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
        return validateAndExtractClaims(token).get("role", String.class);
    }

    public String extractFullName(String token) {
        return validateAndExtractClaims(token).get("fullName", String.class);
    }

    /**
     * Safe validation wrapper — returns false instead of throwing.
     * Use only for the filter's guard check before parsing claims.
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