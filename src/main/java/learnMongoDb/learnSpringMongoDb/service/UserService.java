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

    // --- Phase 2 Properties ---
    @Value("${security.login.progressive-delay.enabled:true}")
    private boolean progressiveDelayEnabled;

    @Value("${security.login.progressive-delay.base-delay-seconds:2}")
    private int baseDelaySeconds;

    @Value("${security.login.progressive-delay.max-delay-seconds:30}")
    private int maxDelaySeconds;

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder(12);
    private static final String DUMMY_HASH = "$2a$12$wN1Q/Xz.iRzX0J6n.VfM.O7zX.x.x.x.x.x.x.x.x.x.x.x.x.x.x";

    public User createUser(User user) {
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

        // 3. Progressive delay check (Phase 2 Update)
        if (user.getNextLoginAllowedAt() != null && user.getNextLoginAllowedAt().isAfter(Instant.now())) {
            long retryAfter = Duration.between(Instant.now(), user.getNextLoginAllowedAt()).getSeconds();
            throw new LoginTooSoonException(retryAfter);
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

            // Phase 2: Always track last failed login timestamp
            secondaryUpdate.set("lastFailedLoginAt", Instant.now());
            boolean needsSecondaryUpdate = true;

            if (attempts >= maxFailedAttempts) {
                // Trigger Hard Lockout
                int newLockoutCount = updatedUser.getLockoutCount() + 1;
                secondaryUpdate.set("lockoutCount", newLockoutCount);

                int lockMinutes = (newLockoutCount == 1) ? 1 : lockDurationMinutes;
                secondaryUpdate.set("lockedUntil", Instant.now().plus(Duration.ofMinutes(lockMinutes)));
            } else if (progressiveDelayEnabled && attempts > 1) {
                // Trigger Progressive Delay (Attempt 2=2s, 3=4s, 4=8s...)
                int delaySeconds = (int) (baseDelaySeconds * Math.pow(2, attempts - 2));
                delaySeconds = Math.min(delaySeconds, maxDelaySeconds); // Cap at 30 seconds
                secondaryUpdate.set("nextLoginAllowedAt", Instant.now().plusSeconds(delaySeconds));
            } else {
                // Just saving lastFailedLoginAt for Attempt 1
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
                .unset("nextLoginAllowedAt")
                .unset("lastFailedLoginAt"); // Phase 2: Clear last failure time
        mongoTemplate.updateFirst(query, update, User.class);
    }

    // --- Profile & Account Recovery Methods ---
    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }

    public User updateUserProfile(String id, String firstName, String lastName, String phoneNumber, String password, Address address) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (firstName != null && !firstName.isBlank()) user.setFirstName(firstName);
        if (lastName != null && !lastName.isBlank()) user.setLastName(lastName);
        if (phoneNumber != null && !phoneNumber.isBlank() && !phoneNumber.equals(user.getPhoneNumber())) {
            if (userRepository.findByPhoneNumber(phoneNumber).isPresent()) {
                throw new PhoneAlreadyExistsException("Phone number is already associated with another account.");
            }
            user.setPhoneNumber(phoneNumber);
        }
        if (password != null && !password.isBlank()) {
            user.setPasswordHash(PASSWORD_ENCODER.encode(password));
        }
        if (address != null) user.setAddress(address);
        return userRepository.save(user);
    }

    public void deleteUser(String id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found");
        }
        userRepository.deleteById(id);
    }

    public void emailExists(String email) {
        if (userRepository.findByEmail(email).isEmpty()) {
            throw new ResourceNotFoundException("No account found with that email.");
        }
    }

    public boolean verifyIdentity(String email, String phoneNumber) {
        return userRepository.findByEmail(email)
                .map(user -> user.getPhoneNumber() != null && user.getPhoneNumber().equals(phoneNumber))
                .orElse(false);
    }

    public void resetPassword(String email, String phoneNumber, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No account found with that email."));
        if (user.getPhoneNumber() == null || !user.getPhoneNumber().equals(phoneNumber)) {
            throw new IllegalArgumentException("The details provided do not match our records.");
        }
        user.setPasswordHash(PASSWORD_ENCODER.encode(newPassword));
        resetLoginCounters(user.getId());
        userRepository.save(user);
    }
}