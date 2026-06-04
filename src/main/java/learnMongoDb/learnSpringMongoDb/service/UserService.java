package learnMongoDb.learnSpringMongoDb.service;

import learnMongoDb.learnSpringMongoDb.entity.User;
import learnMongoDb.learnSpringMongoDb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User createUser(User user) {
        // Check if email is already taken before saving
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            throw new RuntimeException("Email already in use! Please login or use a different email.");
        }

        // Assign a default role if the user didn't specify one
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("USER");
        }

        return userRepository.save(user);
    }

    public User loginUser(String email, String password) {
        // 1. Find the user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with this email."));

        // 2. Check if the password matches (Note: This is plain text for prototyping!)
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Invalid password.");
        }

        // 3. If everything is correct, return the user data
        return user;
    }

    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }
}