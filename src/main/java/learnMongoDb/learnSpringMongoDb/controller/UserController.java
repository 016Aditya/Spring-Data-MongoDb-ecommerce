package learnMongoDb.learnSpringMongoDb.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import learnMongoDb.learnSpringMongoDb.dto.UserDto;
import learnMongoDb.learnSpringMongoDb.entity.User;
import learnMongoDb.learnSpringMongoDb.security.CustomUserDetails;
import learnMongoDb.learnSpringMongoDb.security.JwtUtil;
import learnMongoDb.learnSpringMongoDb.service.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final ModelMapper modelMapper; // ── Inject ModelMapper ──

    // ── REGISTRATION & LOGIN ──────────────────────────────────────────

    @PostMapping("/register")
    public ResponseEntity<UserDto.Response> registerUser(
            @Valid @RequestBody UserDto.Request request) {

        // ModelMapper maps the DTO to the Entity, including password -> passwordHash
        User userToSave = modelMapper.map(request, User.class);

        User savedUser = userService.createUser(userToSave);
        return ResponseEntity.ok(modelMapper.map(savedUser, UserDto.Response.class));
    }

    @PostMapping("/login")
    public ResponseEntity<UserDto.LoginResponse> loginUser(
            @Valid @RequestBody LoginRequest loginRequest) {

        User loggedInUser = userService.loginUser(
                loginRequest.getEmail(),
                loginRequest.getPassword());

        String firstName = loggedInUser.getFirstName() != null ? loggedInUser.getFirstName() : "";
        String lastName  = loggedInUser.getLastName()  != null ? loggedInUser.getLastName()  : "";
        String fullName  = (firstName + " " + lastName).trim();

        String token = jwtUtil.generateToken(
                loggedInUser.getId(),
                loggedInUser.getEmail(),
                loggedInUser.getRole(),
                fullName);

        UserDto.LoginResponse body = new UserDto.LoginResponse();
        body.setToken(token);
        body.setUser(modelMapper.map(loggedInUser, UserDto.Response.class));

        return ResponseEntity.ok(body);
    }

    // ── PROFILE MANAGEMENT ────────────────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails principal) {

        if (!principal.getUserId().equals(id)) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }

        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok(modelMapper.map(user, UserDto.Response.class)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUserProfile(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody UserDto.UpdateProfileRequest request) {

        if (!principal.getUserId().equals(id)) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }

        // Keeping your existing field-by-field pass to the service here
        // since it handles complex null-checking and partial updates perfectly.
        User updatedUser = userService.updateUserProfile(
                id,
                request.getFirstName(),
                request.getLastName(),
                request.getPhoneNumber(),
                request.getPassword());

        return ResponseEntity.ok(modelMapper.map(updatedUser, UserDto.Response.class));
    }

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

    // ── PASSWORD RECOVERY ─────────────────────────────────────────────

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody UserDto.ForgotPasswordRequest request) {
        userService.emailExists(request.getEmail());
        return ResponseEntity.ok(Map.of(
                "message", "If an account with that email exists, you can proceed."));
    }

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

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody UserDto.ResetPasswordRequest request) {

        userService.resetPassword(
                request.getEmail(),
                request.getPhoneNumber(),
                request.getNewPassword());

        return ResponseEntity.ok(Map.of("message", "Password reset successfully."));
    }

    @Data
    public static class LoginRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        private String email;

        @NotBlank(message = "Password is required")
        private String password;
    }
}