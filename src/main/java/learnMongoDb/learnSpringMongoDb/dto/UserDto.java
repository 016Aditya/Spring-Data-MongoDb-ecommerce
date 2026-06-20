package learnMongoDb.learnSpringMongoDb.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

public class UserDto {

    // ─── Registration ────────────────────────────────────────────────────────

    @Data
    public static class Request {

        @NotBlank(message = "First name is required")
        private String firstName;

        @NotBlank(message = "Last name is required")
        private String lastName;

        @Email(message = "Invalid email address")
        @NotBlank(message = "Email is required")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        private String password;

        /**
         * 10-digit phone number. Stored for password-recovery identity
         * verification only – no SMS / OTP is sent.
         */
        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
        private String phoneNumber;
    }

    // ─── Response ────────────────────────────────────────────────────────────
    // phoneNumber is included so the frontend can pre-fill the profile form.
    // passwordHash is NEVER included.

    @Data
    public static class Response {
        private String id;
        private String firstName;
        private String lastName;
        private String email;
        private String phoneNumber;
        private String role;
        private LocalDateTime createdAt;
    }

    // ─── Profile update ──────────────────────────────────────────────────────

    @Data
    public static class UpdateProfileRequest {

        @NotBlank(message = "First name is required")
        private String firstName;

        @NotBlank(message = "Last name is required")
        private String lastName;

        /**
         * 10-digit phone number – optional, but if provided must be valid.
         * Leave null/blank to keep the existing value.
         */
        @Pattern(regexp = "^([0-9]{10})?$", message = "Phone number must be exactly 10 digits")
        private String phoneNumber;

        /** Optional: leave blank to keep existing password. */
        @Size(min = 8, message = "Password must be at least 8 characters")
        private String password;
    }

    // ─── Forgot Password – Step 1: submit email ──────────────────────────────

    @Data
    public static class ForgotPasswordRequest {

        @Email(message = "Invalid email address")
        @NotBlank(message = "Email is required")
        private String email;
    }

    // ─── Forgot Password – Step 2: verify identity (email + phone) ───────────

    @Data
    public static class VerifyIdentityRequest {

        @Email(message = "Invalid email address")
        @NotBlank(message = "Email is required")
        private String email;

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
        private String phoneNumber;
    }

    // ─── Forgot Password – Step 3: set new password ──────────────────────────

    @Data
    public static class ResetPasswordRequest {

        @Email(message = "Invalid email address")
        @NotBlank(message = "Email is required")
        private String email;

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
        private String phoneNumber;

        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "New password must be at least 8 characters")
        private String newPassword;
    }
}
