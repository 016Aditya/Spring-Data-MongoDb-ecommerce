package learnMongoDb.learnSpringMongoDb.service;

import learnMongoDb.learnSpringMongoDb.dto.UserDto;
import learnMongoDb.learnSpringMongoDb.entity.Address;
import learnMongoDb.learnSpringMongoDb.entity.User;
import learnMongoDb.learnSpringMongoDb.error.EmailAlreadyExistsException;
import learnMongoDb.learnSpringMongoDb.error.InvalidCredentialsException;
import learnMongoDb.learnSpringMongoDb.error.PhoneAlreadyExistsException;
import learnMongoDb.learnSpringMongoDb.error.ResourceNotFoundException;
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
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Incorrect email or password."));

        if (!PASSWORD_ENCODER.matches(rawPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException("Incorrect email or password.");
        }
        return user;
    }

    /**
     * updateUserProfile — persists all editable profile fields including address.
     *
     * @param id          MongoDB user document ID
     * @param firstName   Required — from UpdateProfileRequest
     * @param lastName    Required — from UpdateProfileRequest
     * @param phoneNumber Optional — only updated when non-blank and different from current
     * @param rawPassword Optional — only updated when non-blank
     * @param addressReq  Optional — when non-null, replaces the embedded address sub-document
     */
    public User updateUserProfile(
            String id,
            String firstName,
            String lastName,
            String phoneNumber,
            String rawPassword,
            UserDto.AddressRequest addressReq) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        user.setFirstName(firstName);
        user.setLastName(lastName);

        // Phone — only update if changed; guard against duplicates
        if (phoneNumber != null && !phoneNumber.isBlank() &&
                !phoneNumber.equals(user.getPhoneNumber())) {
            userRepository.findByPhoneNumber(phoneNumber)
                    .ifPresent(existing -> {
                        throw new PhoneAlreadyExistsException("Phone number already in use.");
                    });
            user.setPhoneNumber(phoneNumber);
        }

        // Password — only re-hash when a new value is explicitly supplied
        if (rawPassword != null && !rawPassword.isBlank()) {
            user.setPasswordHash(PASSWORD_ENCODER.encode(rawPassword));
        }

        // Address — replace the embedded sub-document when provided
        if (addressReq != null) {
            Address address = Address.builder()
                    .fullName(addressReq.getFullName())
                    .phoneNumber(addressReq.getPhoneNumber())
                    .addressLine1(addressReq.getAddressLine1())
                    .addressLine2(addressReq.getAddressLine2())
                    .city(addressReq.getCity())
                    .state(addressReq.getState())
                    .zipCode(addressReq.getZipCode())
                    .country(addressReq.getCountry())
                    .build();
            user.setAddress(address);
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
                .orElseThrow(() -> new InvalidCredentialsException("Identity verification failed."));

        user.setPasswordHash(PASSWORD_ENCODER.encode(rawNewPassword));
        userRepository.save(user);
    }
}