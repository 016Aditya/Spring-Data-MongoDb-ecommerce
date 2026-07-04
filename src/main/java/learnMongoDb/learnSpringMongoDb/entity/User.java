package learnMongoDb.learnSpringMongoDb.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    private String firstName;
    private String lastName;

    @Indexed(unique = true)
    private String email;

    // BCrypt hash — NEVER the raw password.
    private String passwordHash;

    // 10-digit mobile number stored for password-recovery identity verification
    private String phoneNumber;
    private String role;
    private Address address;

    // --- Security Hardening Fields ---
    private int failedLoginAttempts;
    private int lockoutCount;
    private Instant lockedUntil;
    private Instant nextLoginAllowedAt;

    // Phase 2: Track exact time of last failure
    private Instant lastFailedLoginAt;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}