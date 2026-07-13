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

/**
 * Address — standalone MongoDB document.
 *
 * Each address belongs to one user (userId) and is stored in the
 * "addresses" collection. This replaces the old embedded Address
 * value object that was nested inside Order documents.
 *
 * The Order entity still embeds a snapshot of the address at the time
 * of placing the order (fullName, addressLine1, …) to preserve order
 * history even when the user later modifies or deletes the address.
 */
@Document(collection = "addresses")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Address {

    @Id
    private String id;

    /** FK — references users._id */
    @Indexed
    private String userId;

    // ── Address fields (mirrors AddressRequest) ──────────────────────────────
    private String fullName;
    private String phoneNumber;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String zipCode;
    private String country;

    /**
     * Whether this is the user's default delivery address.
     * Only one address per user may have defaultAddress = true.
     * The service enforces this invariant on create / update / setDefault.
     */
    @Builder.Default
    private boolean defaultAddress = false;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}