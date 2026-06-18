package learnMongoDb.learnSpringMongoDb.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import learnMongoDb.learnSpringMongoDb.dto.UserDto;
import learnMongoDb.learnSpringMongoDb.entity.User;
import learnMongoDb.learnSpringMongoDb.service.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * UserController
 *
 * Endpoints
 * ----------
 * POST   /api/users/register          - Create account
 * POST   /api/users/login             - Authenticate
 * GET    /api/users/{id}              - Get profile by ID
 * PUT    /api/users/{id}              - Update profile
 * DELETE /api/users/{id}             - Delete account
 *
 * Forgot Password (no tokens / SMS)
 * POST   /api/users/forgot-password  - Step 1: check email exists
 * POST   /api/users/verify-identity  - Step 2: email + phone must match
 * POST   /api/users/reset-password   - Step 3: set new password
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Register ---------------------------------------------------------------

    @PostMapping("/register")
    public ResponseEntity<UserDto.Response> registerUser(
            @Valid @RequestBody UserDto.Request request) {

        User userToSave = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .passwordHash(request.getPassword())
                .phoneNumber(request.getPhoneNumber())
                .build();

        User savedUser = userService.createUser(userToSave);
        return ResponseEntity.ok(mapToResponse(savedUser));
    }

    // Login ------------------------------------------------------------------

    @PostMapping("/login")
    public ResponseEntity<UserDto.Response> loginUser(
            @Valid @RequestBody LoginRequest loginRequest) {

        User loggedInUser = userService.loginUser(
                loginRequest.getEmail(),
                loginRequest.getPassword());

        return ResponseEntity.ok(mapToResponse(loggedInUser));
    }

    // Get by ID --------------------------------------------------------------

    @GetMapping("/{id}")
    public ResponseEntity<UserDto.Response> getUserById(@PathVariable String id) {
        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok(mapToResponse(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    // Update profile ---------------------------------------------------------

    @PutMapping("/{id}")
    public ResponseEntity<UserDto.Response> updateUserProfile(
            @PathVariable String id,
            @Valid @RequestBody UserDto.UpdateProfileRequest request) {

        User updatedUser = userService.updateUserProfile(
                id,
                request.getFirstName(),
                request.getLastName(),
                request.getPassword());

        return ResponseEntity.ok(mapToResponse(updatedUser));
    }

    // Delete -----------------------------------------------------------------

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // Forgot Password - Step 1 -----------------------------------------------

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody UserDto.ForgotPasswordRequest request) {
        userService.emailExists(request.getEmail());
        return ResponseEntity.ok(Map.of(
                "message", "If an account with that email exists, you can proceed."));
    }

    // Forgot Password - Step 2 -----------------------------------------------

    @PostMapping("/verify-identity")
    public ResponseEntity<Map<String, Object>> verifyIdentity(
            @Valid @RequestBody UserDto.VerifyIdentityRequest request) {

        boolean verified = userService.verifyIdentity(
                request.getEmail(),
                request.getPhoneNumber());

        if (!verified) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "verified", false,
                            "message", "The details provided do not match our records."));
        }

        return ResponseEntity.ok(Map.of(
                "verified", true,
                "message", "Identity verified. You may now set a new password."));
    }

    // Forgot Password - Step 3 -----------------------------------------------

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody UserDto.ResetPasswordRequest request) {

        userService.resetPassword(
                request.getEmail(),
                request.getPhoneNumber(),
                request.getNewPassword());

        return ResponseEntity.ok(Map.of("message", "Password reset successfully."));
    }

    // Helpers ----------------------------------------------------------------

    private UserDto.Response mapToResponse(User user) {
        UserDto.Response response = new UserDto.Response();
        response.setId(user.getId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }

    // Inline Login DTO -------------------------------------------------------

    /**
     * Root cause fix: added @Valid on the handler parameter AND @NotBlank
     * on both fields. Without @Valid the constraints are never evaluated
     * and null values silently reach UserService.loginUser(), causing NPE.
     */
    @Data
    public static class LoginRequest {

        @NotBlank(message = "Email is required")
        private String email;

        @NotBlank(message = "Password is required")
        private String password;
    }
}
