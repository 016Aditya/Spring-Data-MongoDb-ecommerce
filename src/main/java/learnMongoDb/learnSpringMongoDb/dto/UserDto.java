package learnMongoDb.learnSpringMongoDb.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import learnMongoDb.learnSpringMongoDb.entity.Address;
import lombok.Data;

import java.time.Instant;

public class UserDto {

    // ── Registration request ──────────────────────────────────────────────────
    @Data
    public static class Request {
        @NotBlank(message = "First name is required")
        private String firstName;

        @NotBlank(message = "Last name is required")
        private String lastName;

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        private String email;

        @NotBlank(message = "Password is required")
        @Pattern(
                regexp = "^(?=(?:.*\\d){2,})(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$",
                message = "Password must be at least 8 characters and contain one uppercase letter, one lowercase letter, two numbers, and one special character."
        )
        private String password;

        @NotBlank(message = "Phone number is required")
        @Pattern(
                regexp = "^[6-9]\\d{9}$",
                message = "Enter a valid Indian mobile number."
        )
        private String phoneNumber;
    }

    // ── Update-profile request ────────────────────────────────────────────────
    @Data
    public static class UpdateProfileRequest {
        @NotBlank(message = "First name is required")
        private String firstName;

        @NotBlank(message = "Last name is required")
        private String lastName;

        @Pattern(
                regexp = "^$|^[6-9]\\d{9}$",
                message = "Enter a valid Indian mobile number."
        )
        private String phoneNumber;

        @Pattern(
                regexp = "^$|^(?=(?:.*\\d){2,})(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$",
                message = "Password must be at least 8 characters and contain one uppercase letter, one lowercase letter, two numbers, and one special character."
        )
        private String password;

        // Optional embedded address — validated only when present
        @Valid
        private AddressRequest address;
    }

    // ── Address sub-DTO (used inside UpdateProfileRequest) ────────────────────
    @Data
    public static class AddressRequest {
        private String fullName;
        private String phoneNumber;
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String zipCode;
        private String country;
    }

    // ── Forgot-password ───────────────────────────────────────────────────────
    @Data
    public static class ForgotPasswordRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        private String email;
    }

    // ── Verify-identity ───────────────────────────────────────────────────────
    @Data
    public static class VerifyIdentityRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        private String email;

        @NotBlank(message = "Phone number is required")
        @Pattern(
                regexp = "^[6-9]\\d{9}$",
                message = "Enter a valid Indian mobile number."
        )
        private String phoneNumber;
    }

    // ── Reset-password ────────────────────────────────────────────────────────
    @Data
    public static class ResetPasswordRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        private String email;

        @NotBlank(message = "Phone number is required")
        @Pattern(
                regexp = "^[6-9]\\d{9}$",
                message = "Enter a valid Indian mobile number."
        )
        private String phoneNumber;

        @NotBlank(message = "New password is required")
        @Pattern(
                regexp = "^(?=(?:.*\\d){2,})(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$",
                message = "Password must be at least 8 characters and contain one uppercase letter, one lowercase letter, two numbers, and one special character."
        )
        private String newPassword;
    }

    // ── Profile response (GET + PUT response body) ────────────────────────────
    @Data
    public static class Response {
        private String id;
        private String firstName;
        private String lastName;
        /** Convenience field — firstName + " " + lastName, set by the controller */
        private String fullName;
        private String email;
        private String phoneNumber;
        private String role;
        private Instant createdAt;
        /** Embedded address — null if the user has never saved one */
        private Address address;
    }

    // ── Login response ────────────────────────────────────────────────────────
    @Data
    public static class LoginResponse {
        private String token;
        private Response user;
    }
}