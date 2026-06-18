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

import java.time.LocalDateTime;

/**
 * User entity.
 *
 * Security notes
 * ──────────────
 * • passwordHash  – BCrypt-hashed password. Never store / return plain text.
 * • phoneNumber   – Stored for Forgot-Password identity verification ONLY.
 *                   No SMS / OTP integration is used.
 */
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

    /** BCrypt hash – NEVER the raw password. */
    private String passwordHash;

    /**
     * 10-digit mobile number stored for password-recovery identity
     * verification only.  No SMS / OTP is sent.
     */
    private String phoneNumber;

    private String role;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
