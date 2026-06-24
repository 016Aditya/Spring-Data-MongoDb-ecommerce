package learnMongoDb.learnSpringMongoDb.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.Instant;

public class UserDto {

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
        @Pattern(
                regexp = "^[6-9]\\d{9}$",
                message = "Enter a valid Indian mobile number."
        )
        private String phoneNumber;
    }

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

    @Data
    public static class Response {
        private String id;
        private String firstName;
        private String lastName;
        private String email;
        private String phoneNumber;
        private String role;
        private Instant createdAt;
    }

    @Data
    public static class LoginResponse {
        private String token;
        private Response user;
    }
}