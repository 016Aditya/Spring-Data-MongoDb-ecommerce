package learnMongoDb.learnSpringMongoDb.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * CustomUserDetails
 *
 * Wraps the userId and role extracted from the validated JWT.
 * This is what Spring Security stores in the SecurityContext and what
 * controllers receive via @AuthenticationPrincipal.
 *
 * Note: there is no password here — authentication already happened
 * at the JWT validation step. This object only carries identity.
 */
public class CustomUserDetails implements UserDetails {

    private final String userId;
    private final String role;

    public CustomUserDetails(String userId, String role) {
        this.userId = userId;
        this.role   = role;
    }

    /**
     * Returns the MongoDB _id of the authenticated user.
     * Controllers cast @AuthenticationPrincipal to CustomUserDetails
     * and call this method to get the trusted userId.
     */
    public String getUserId() {
        return userId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Spring Security convention: role strings must be prefixed with ROLE_
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override public String getPassword()  { return null; }   // not used after JWT validation
    @Override public String getUsername()  { return userId; } // userId doubles as the principal name
    @Override public boolean isAccountNonExpired()  { return true; }
    @Override public boolean isAccountNonLocked()   { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()            { return true; }
}
