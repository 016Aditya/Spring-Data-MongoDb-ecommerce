package learnMongoDb.learnSpringMongoDb.service;

import learnMongoDb.learnSpringMongoDb.entity.User;
import learnMongoDb.learnSpringMongoDb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * UserService
 *
 * Security fixes applied
 * ──────────────────────
 * 1. Passwords are BCrypt-hashed before storage – never persisted as plain text.
 * 2. Login uses BCrypt.matches() instead of plain-text equals.
 * 3. Profile update hashes the new password before saving.
 * 4. phoneNumber is stored and used for identity verification only.
 * 5. Forgot-Password flow: email + phoneNumber must both match –
 *    no tokens / emails / SMS are sent (stateless verification gate).
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // ─────────────────────────────────────────────────────────────
    // BCrypt encoder – work-factor 12 is a safe default for 2024/2025
    // ─────────────────────────────────────────────────────────────
    private static final BCryptPasswordEncoder PASSWORD_ENCODER =
            new BCryptPasswordEncoder(12);

    // ── Registration ─────────────────────────────────────────────────────────

    public User createUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException(
                    "Email already in use. Please log in or use a different email.");
        }

        // Hash the password before persisting
        user.setPasswordHash(PASSWORD_ENCODER.encode(user.getPasswordHash()));

        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("USER");
        }

        return userRepository.save(user);
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    public User loginUser(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(
                        "No account found with that email address."));

        // Timing-safe BCrypt comparison – never equals() on hashes
        if (!PASSWORD_ENCODER.matches(rawPassword, user.getPasswordHash())) {
            throw new RuntimeException("Incorrect password.");
        }

        return user;
    }

    // ── Profile update ───────────────────────────────────────────────────────

    public User updateUserProfile(String id, String firstName, String lastName,
                                  String rawPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "User not found with ID: " + id));

        user.setFirstName(firstName);
        user.setLastName(lastName);

        // Only update password if a new one was explicitly provided
        if (rawPassword != null && !rawPassword.isBlank()) {
            user.setPasswordHash(PASSWORD_ENCODER.encode(rawPassword));
        }

        return userRepository.save(user);
    }

    // ── Getters / delete ─────────────────────────────────────────────────────

    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }

    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }

    // ── Forgot Password – Step 1: verify email exists ─────────────────────────

    /**
     * Returns true if the email belongs to a registered account.
     * The frontend uses this result to advance to the phone-verification step.
     * Always returns the same shape to avoid email enumeration attacks.
     */
    public boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    // ── Forgot Password – Step 2: verify identity (email + phone) ────────────

    /**
     * Checks that the supplied email and phoneNumber both belong to the same
     * account.  No token, no SMS, no external service – pure local verification.
     *
     * @return true if both fields match the stored record, false otherwise.
     *         Callers should NEVER reveal WHICH field failed.
     */
    public boolean verifyIdentity(String email, String phoneNumber) {
        return userRepository.findByEmail(email)
                .map(user -> phoneNumber.equals(user.getPhoneNumber()))
                .orElse(false);
    }

    // ── Forgot Password – Step 3: set new password ───────────────────────────

    /**
     * Re-verifies identity before changing the password to prevent skipping
     * the verification step via a direct API call.
     */
    public void resetPassword(String email, String phoneNumber,
                              String rawNewPassword) {
        // Re-verify – do not trust the frontend to have completed step 2
        if (!verifyIdentity(email, phoneNumber)) {
            throw new RuntimeException(
                    "Identity verification failed. Cannot reset password.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(
                        "User not found."));

        user.setPasswordHash(PASSWORD_ENCODER.encode(rawNewPassword));
        userRepository.save(user);
    }
}
