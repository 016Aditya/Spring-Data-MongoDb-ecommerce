package learnMongoDb.learnSpringMongoDb.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * AddressDto
 *
 * Contains the inbound request record (AddressRequest) and the
 * outbound response record (AddressResponse) as static inner classes,
 * following the same DTO pattern used by UserDto / OrderDto in this project.
 */
public class AddressDto {

    // ── Inbound ──────────────────────────────────────────────────────────────

    /**
     * Payload for POST /api/v1/addresses  and  PUT /api/v1/addresses/{id}
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddressRequest {

        @NotBlank(message = "Full name is required")
        @Size(max = 100, message = "Full name must be at most 100 characters")
        private String fullName;

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^[+]?[0-9\\s\\-]{7,15}$", message = "Invalid phone number")
        private String phoneNumber;

        @NotBlank(message = "Address line 1 is required")
        @Size(max = 200, message = "Address line 1 must be at most 200 characters")
        private String addressLine1;

        @Size(max = 200, message = "Address line 2 must be at most 200 characters")
        private String addressLine2;

        @NotBlank(message = "City is required")
        private String city;

        @NotBlank(message = "State is required")
        private String state;

        @NotBlank(message = "Zip code is required")
        @Pattern(regexp = "^[A-Za-z0-9\\s\\-]{3,10}$", message = "Invalid zip code")
        private String zipCode;

        private String country;

        /** If true, this address becomes the default; all others are unset. */
        private boolean defaultAddress;
    }

    // ── Outbound ─────────────────────────────────────────────────────────────

    /**
     * Returned for every address endpoint response.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddressResponse {
        private String    id;
        private String    userId;
        private String    fullName;
        private String    phoneNumber;
        private String    addressLine1;
        private String    addressLine2;
        private String    city;
        private String    state;
        private String    zipCode;
        private String    country;
        private boolean   defaultAddress;
        private Instant   createdAt;
        private Instant   updatedAt;
    }
}