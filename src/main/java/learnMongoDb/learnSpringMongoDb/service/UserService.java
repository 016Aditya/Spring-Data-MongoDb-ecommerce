package learnMongoDb.learnSpringMongoDb.service;

import learnMongoDb.learnSpringMongoDb.dto.UserDto;
import learnMongoDb.learnSpringMongoDb.entity.Address;
import learnMongoDb.learnSpringMongoDb.entity.User;
import learnMongoDb.learnSpringMongoDb.error.EmailAlreadyExistsException;
import learnMongoDb.learnSpringMongoDb.error.InvalidCredentialsException;
import learnMongoDb.learnSpringMongoDb.error.PhoneAlreadyExistsException;
import learnMongoDb.learnSpringMongoDb.error.ResourceNotFoundException;
import learnMongoDb.learnSpringMongoDb.error.AccountLockedException;
import learnMongoDb.learnSpringMongoDb.error.LoginTooSoonException;
import learnMongoDb.learnSpringMongoDb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate;

    @Value("${security.login.max-failed-attempts:5}")
    private int maxFailedAttempts;

    @Value("${security.login.lock-duration-minutes:15}")
    private int lockDurationMinutes;

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder(12);

    // Formatted for BCrypt(12) so it occupies the exact same CPU time as a real check
    private static final String DUMMY_HASH = "$2a$12$wN1Q/Xz.iRzX0J6n.VfM.O7zX.x.x.x.x.x.x.x.x.x.x.x.x.x.x";

    public User createUser(User user) {
        // ... (Keep existing createUser logic exactly as is)
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Email already in use.");
        }
        if (userRepository.findByPhoneNumber(user.getPhoneNumber()).isPresent()) {
            throw new PhoneAlreadyExistsException("Phone number is already associated with another account.");
        }
        user.setPasswordHash(PASSWORD_ENCODER.encode(user.getPasswordHash()));
        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("USER");
        }
        return userRepository.save(user);
    }

    public User loginUser(String email, String rawPassword) {
        User user = userRepository.findByEmail(email).orElse(null);

        // 1. Constant-time execution against timing attacks (User enumeration)
        if (user == null) {
            PASSWORD_ENCODER.matches(rawPassword, DUMMY_HASH);
            throw new InvalidCredentialsException("Incorrect email or password.");
        }

        // 2. Hard-lockout check
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now())) {
            throw new AccountLockedException("Account is locked. Try again later.");
        }

        // 3. Progressive delay check
        if (user.getNextLoginAllowedAt() != null && user.getNextLoginAllowedAt().isAfter(Instant.now())) {
            long remainingSeconds = Duration.between(Instant.now(), user.getNextLoginAllowedAt()).getSeconds();
            throw new LoginTooSoonException("Too many attempts.", remainingSeconds);
        }

        // 4. Verify Password
        if (!PASSWORD_ENCODER.matches(rawPassword, user.getPasswordHash())) {
            handleFailedLogin(user);
            throw new InvalidCredentialsException("Incorrect email or password.");
        }

        // 5. Successful login -> Clear locks and counters
        resetLoginCounters(user.getId());
        return user;
    }

    private void handleFailedLogin(User user) {
        Query query = new Query(Criteria.where("id").is(user.getId()));

        // Atomically increment failed attempts
        Update update = new Update().inc("failedLoginAttempts", 1);
        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true);
        User updatedUser = mongoTemplate.findAndModify(query, update, options, User.class);

        if (updatedUser != null) {
            int attempts = updatedUser.getFailedLoginAttempts();
            Update secondaryUpdate = new Update();
            boolean needsSecondaryUpdate = false;

            if (attempts >= maxFailedAttempts) {
                // Trigger Lockout
                int newLockoutCount = updatedUser.getLockoutCount() + 1;
                secondaryUpdate.set("lockoutCount", newLockoutCount);

                int lockMinutes = (newLockoutCount == 1) ? 1 : lockDurationMinutes;
                secondaryUpdate.set("lockedUntil", Instant.now().plus(Duration.ofMinutes(lockMinutes)));
                needsSecondaryUpdate = true;
            } else {
                // Trigger Progressive Delay (2s, 4s, 8s, 16s...)
                int delaySeconds = (int) Math.pow(2, attempts);
                delaySeconds = Math.min(delaySeconds, 30); // Cap at 30 seconds
                secondaryUpdate.set("nextLoginAllowedAt", Instant.now().plusSeconds(delaySeconds));
                needsSecondaryUpdate = true;
            }

            if (needsSecondaryUpdate) {
                mongoTemplate.updateFirst(query, secondaryUpdate, User.class);
            }
        }
    }

    private void resetLoginCounters(String userId) {
        Query query = new Query(Criteria.where("id").is(userId));
        Update update = new Update()
                .set("failedLoginAttempts", 0)
                .set("lockoutCount", 0)
                .unset("lockedUntil")
                .unset("nextLoginAllowedAt");
        mongoTemplate.updateFirst(query, update, User.class);
    }

    // ... (Keep existing updateUserProfile, getUserById, deleteUser, emailExists, verifyIdentity, resetPassword)
}