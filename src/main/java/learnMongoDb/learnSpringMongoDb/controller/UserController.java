package learnMongoDb.learnSpringMongoDb.controller;

import jakarta.servlet.http.HttpServletRequest;
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
    private final ModelMapper modelMapper;

    // ── REGISTRATION & LOGIN ──────────────────────────────────────────────────

    @PostMapping("/register")
    public ResponseEntity<UserDto.Response> registerUser(
            @Valid @RequestBody UserDto.Request request) {

        User userToSave = modelMapper.map(request, User.class);
        User savedUser  = userService.createUser(userToSave);
        return ResponseEntity.ok(toResponse(savedUser));
    }

    @PostMapping("/login")
    public ResponseEntity<UserDto.LoginResponse> loginUser(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest httpRequest) {

        // 1. Extract Client IP
        String ipAddress = getClientIp(httpRequest);

        // 2. Authenticate and enforce security policies
        User loggedInUser = userService.loginUser(
                loginRequest.getEmail(),
                loginRequest.getPassword(),
                ipAddress);

        // 3. Generate JWT
        String token = jwtUtil.generateToken(
                loggedInUser.getId(),
                loggedInUser.getEmail(),
                loggedInUser.getRole(),
                toFullName(loggedInUser));

        // 4. Build Frontend-Compatible Response
        UserDto.LoginResponse body = new UserDto.LoginResponse();
        body.setToken(token);
        body.setUser(toResponse(loggedInUser));

        return ResponseEntity.ok(body);
    }

    // ── PROFILE MANAGEMENT ────────────────────────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails principal) {

        if (!principal.getUserId().equals(id)) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }

        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok(toResponse(user)))
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

        User updatedUser = userService.updateUserProfile(
                id,
                request.getFirstName(),
                request.getLastName(),
                request.getPhoneNumber(),
                request.getPassword(),
                request.getAddress());

        return ResponseEntity.ok(toResponse(updatedUser));
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

    // ── PASSWORD RECOVERY ─────────────────────────────────────────────────────

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

    // ── Private helpers ───────────────────────────────────────────────────────

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Maps a User entity to UserDto.Response and populates the convenience
     * `fullName` field that the frontend reads directly.
     */
    private UserDto.Response toResponse(User user) {
        UserDto.Response resp = modelMapper.map(user, UserDto.Response.class);
        resp.setFullName(toFullName(user));
        return resp;
    }

    private String toFullName(User user) {
        String first = user.getFirstName() != null ? user.getFirstName() : "";
        String last  = user.getLastName()  != null ? user.getLastName()  : "";
        return (first + " " + last).trim();
    }

    // ── Inner login-request DTO ───────────────────────────────────────────────
    @Data
    public static class LoginRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        private String email;

        @NotBlank(message = "Password is required")
        private String password;
    }
}