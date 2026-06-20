package learnMongoDb.learnSpringMongoDb.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import learnMongoDb.learnSpringMongoDb.dto.UserDto;
import learnMongoDb.learnSpringMongoDb.entity.User;
import learnMongoDb.learnSpringMongoDb.security.CustomUserDetails;
import learnMongoDb.learnSpringMongoDb.security.JwtUtil;
import learnMongoDb.learnSpringMongoDb.service.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * UserController
 *
 * Endpoints
 * ----------
 * POST   /api/users/register          - Create account
 * POST   /api/users/login             - Authenticate → returns JWT
 * GET    /api/users/{id}              - Get profile (own profile only)
 * PUT    /api/users/{id}              - Update profile (own profile only)
 * DELETE /api/users/{id}             - Delete account (own profile only)
 *
 * Forgot Password (no tokens / SMS)
 * POST   /api/users/forgot-password  - Step 1: check email exists
 * POST   /api/users/verify-identity  - Step 2: email + phone must match
 * POST   /api/users/reset-password   - Step 3: set new password
 *
 * Security changes:
 * - login() now issues a signed JWT containing userId (sub), email, role.
 * - Protected endpoints extract userId from the JWT principal, not the URL path,
 *   and reject requests where the path userId doesn't match the authenticated userId.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil    jwtUtil;

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

    // Login — issues JWT -----------------------------------------------------

    @PostMapping("/login")
    public ResponseEntity<UserDto.LoginResponse> loginUser(
            @Valid @RequestBody LoginRequest loginRequest) {

        User loggedInUser = userService.loginUser(
                loginRequest.getEmail(),
                loginRequest.getPassword());

        // Generate a signed JWT with userId in the "sub" claim.
        // The frontend stores this token and sends it as Authorization: Bearer <token>.
        String token = jwtUtil.generateToken(
                loggedInUser.getId(),
                loggedInUser.getEmail(),
                loggedInUser.getRole());

        UserDto.LoginResponse body = new UserDto.LoginResponse();
        body.setToken(token);
        body.setUser(mapToResponse(loggedInUser));

        return ResponseEntity.ok(body);
    }

    // Get by ID — own profile only -------------------------------------------

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails principal) {

        if (!principal.getUserId().equals(id)) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }

        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok(mapToResponse(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    // Update profile — own profile only --------------------------------------

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUserProfile(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody UserDto.UpdateProfileRequest request) {

        if (!principal.getUserId().equals(id)) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }

        User updatedUser = userService.updateUserProfile(
                id,
                request.getFirstName(),
                request.getLastName(),
                request.getPhoneNumber(),
                request.getPassword());

        return ResponseEntity.ok(mapToResponse(updatedUser));
    }

    // Delete — own profile only ----------------------------------------------

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails principal) {

        if (!principal.getUserId().equals(id)) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }

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
        response.setPhoneNumber(user.getPhoneNumber());
        response.setRole(user.getRole());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }

    // Inline Login DTO -------------------------------------------------------

    @Data
    public static class LoginRequest {

        @NotBlank(message = "Email is required")
        private String email;

        @NotBlank(message = "Password is required")
        private String password;
    }
}
