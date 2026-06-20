package learnMongoDb.learnSpringMongoDb.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.Instant;

/**
 * UserDto
 *
 * Data Transfer Objects for User API requests and responses.
 *
 * LoginResponse now includes a JWT token so the frontend can
 * store and send it on subsequent requests.
 */
public class UserDto {

    // ── Request DTOs ─────────────────────────────────────────────────────────

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
        private String password;

        private String phoneNumber;
    }

    @Data
    public static class UpdateProfileRequest {
        @NotBlank(message = "First name is required")
        private String firstName;

        @NotBlank(message = "Last name is required")
        private String lastName;

        private String phoneNumber;
        private String password;
    }

    @Data
    public static class ForgotPasswordRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        private String email;
    }

    @Data
    public static class VerifyIdentityRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        private String email;

        @NotBlank(message = "Phone number is required")
        private String phoneNumber;
    }

    @Data
    public static class ResetPasswordRequest {
        @NotBlank private String email;
        @NotBlank private String phoneNumber;
        @NotBlank private String newPassword;
    }

    // ── Response DTOs ────────────────────────────────────────────────────────

    @Data
    public static class Response {
        private String  id;
        private String  firstName;
        private String  lastName;
        private String  email;
        private String  phoneNumber;
        private String  role;
        private Instant createdAt;
    }

    /**
     * LoginResponse
     *
     * Returned by POST /api/users/login.
     * Contains the signed JWT plus the user profile.
     * The frontend must store the token and include it in every subsequent
     * request as:  Authorization: Bearer <token>
     */
    @Data
    public static class LoginResponse {
        private String   token;  // signed JWT
        private Response user;   // user profile (for UI state — never trust client-side for auth)
    }
}
