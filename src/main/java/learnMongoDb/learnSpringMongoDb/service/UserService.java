package learnMongoDb.learnSpringMongoDb.service;

import learnMongoDb.learnSpringMongoDb.entity.User;
import learnMongoDb.learnSpringMongoDb.error.UserNotFoundException;
import learnMongoDb.learnSpringMongoDb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private static final BCryptPasswordEncoder PASSWORD_ENCODER =
            new BCryptPasswordEncoder(12);

    public User createUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already in use.");
        }

        if (userRepository.findByPhoneNumber(user.getPhoneNumber()).isPresent()) {
            throw new RuntimeException("Phone number is already associated with another account.");
        }

        user.setPasswordHash(PASSWORD_ENCODER.encode(user.getPasswordHash()));

        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("USER");
        }

        return userRepository.save(user);
    }

    public User loginUser(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No account found with that email address."));

        if (!PASSWORD_ENCODER.matches(rawPassword, user.getPasswordHash())) {
            throw new RuntimeException("Incorrect password.");
        }
        return user;
    }

    public User updateUserProfile(String id, String firstName, String lastName, String phoneNumber, String rawPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        user.setFirstName(firstName);
        user.setLastName(lastName);

        if (phoneNumber != null && !phoneNumber.isBlank() && !phoneNumber.equals(user.getPhoneNumber())) {
            userRepository.findByPhoneNumber(phoneNumber)
                    .ifPresent(existing -> {
                        throw new RuntimeException("Phone number already in use.");
                    });
            user.setPhoneNumber(phoneNumber);
        }

        if (rawPassword != null && !rawPassword.isBlank()) {
            user.setPasswordHash(PASSWORD_ENCODER.encode(rawPassword));
        }

        return userRepository.save(user);
    }

    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }

    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }

    public boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public boolean verifyIdentity(String email, String phoneNumber) {
        return userRepository.findByEmailAndPhoneNumber(email, phoneNumber).isPresent();
    }

    public void resetPassword(String email, String phoneNumber, String rawNewPassword) {
        User user = userRepository.findByEmailAndPhoneNumber(email, phoneNumber)
                .orElseThrow(() -> new RuntimeException("Identity verification failed."));

        user.setPasswordHash(PASSWORD_ENCODER.encode(rawNewPassword));
        userRepository.save(user);
    }
}