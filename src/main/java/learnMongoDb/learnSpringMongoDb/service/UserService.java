package learnMongoDb.learnSpringMongoDb.service;

import learnMongoDb.learnSpringMongoDb.entity.User;
import learnMongoDb.learnSpringMongoDb.error.UserNotFoundException;
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
 *
 * Bug fixes (this commit)
 * ──────────────────────
 * - updateUserProfile now accepts and persists phoneNumber.
 * - User-not-found now throws UserNotFoundException (→ 404) instead of
 *   RuntimeException (→ 400), so the frontend can distinguish "bad data"
 *   from "wrong endpoint / stale ID".
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private static final BCryptPasswordEncoder PASSWORD_ENCODER =
            new BCryptPasswordEncoder(12);

    // ── Registration ─────────────────────────────────────────────────────────

    public User createUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException(
                    "Email already in use. Please log in or use a different email.");
        }
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
        if (!PASSWORD_ENCODER.matches(rawPassword, user.getPasswordHash())) {
            throw new RuntimeException("Incorrect password.");
        }
        return user;
    }

    // ── Profile update ───────────────────────────────────────────────────────

    /**
     * @param id          MongoDB ObjectId of the user document.
     * @param firstName   New first name (required).
     * @param lastName    New last name (required).
     * @param phoneNumber New phone number (optional – null/blank keeps existing).
     * @param rawPassword New raw password (optional – null/blank keeps existing).
     */
    public User updateUserProfile(String id,
                                  String firstName,
                                  String lastName,
                                  String phoneNumber,
                                  String rawPassword) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with ID: " + id));

        user.setFirstName(firstName);
        user.setLastName(lastName);

        // Only update phoneNumber if explicitly provided
        if (phoneNumber != null && !phoneNumber.isBlank()) {
            user.setPhoneNumber(phoneNumber);
        }

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

    public boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    // ── Forgot Password – Step 2: verify identity (email + phone) ────────────

    public boolean verifyIdentity(String email, String phoneNumber) {
        return userRepository.findByEmail(email)
                .map(user -> phoneNumber.equals(user.getPhoneNumber()))
                .orElse(false);
    }

    // ── Forgot Password – Step 3: set new password ───────────────────────────

    public void resetPassword(String email, String phoneNumber, String rawNewPassword) {
        if (!verifyIdentity(email, phoneNumber)) {
            throw new RuntimeException(
                    "Identity verification failed. Cannot reset password.");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found."));
        user.setPasswordHash(PASSWORD_ENCODER.encode(rawNewPassword));
        userRepository.save(user);
    }
}
