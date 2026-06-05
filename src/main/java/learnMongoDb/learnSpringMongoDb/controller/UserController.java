package learnMongoDb.learnSpringMongoDb.controller;

import learnMongoDb.learnSpringMongoDb.dto.UserDto;
import learnMongoDb.learnSpringMongoDb.entity.User;
import learnMongoDb.learnSpringMongoDb.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserDto.Response> registerUser(@RequestBody UserDto.Request request) {
        // 1. Map the incoming Request DTO to a real User Entity
        User userToSave = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(request.getPassword())
                .build();

        // 2. Let the Service handle the business logic and database save
        User savedUser = userService.createUser(userToSave);

        // 3. Map the saved entity back to a Response DTO (hiding the password!)
        return ResponseEntity.ok(mapToResponse(savedUser));
    }

    @PostMapping("/login")
    public ResponseEntity<UserDto.Response> loginUser(@RequestBody LoginRequest loginRequest) {
        // Service authenticates and returns the full entity
        User loggedInUser = userService.loginUser(loginRequest.getEmail(), loginRequest.getPassword());

        // Map to Response DTO to hide the password before sending to the client
        return ResponseEntity.ok(mapToResponse(loggedInUser));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto.Response> getUserById(@PathVariable String id) {
        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok(mapToResponse(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto.Response> updateUserProfile(
            @PathVariable String id,
            @RequestBody UserDto.UpdateProfileRequest request) {

        User updatedUser = userService.updateUserProfile(
                id,
                request.getFirstName(),
                request.getLastName(),
                request.getPassword()
        );

        return ResponseEntity.ok(mapToResponse(updatedUser));
    }


    // --- Helper Methods & Classes ---

    // Translates a database User into a safe JSON Response
    private UserDto.Response mapToResponse(User user) {
        UserDto.Response response = new UserDto.Response();
        response.setId(user.getId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }

    // Lightweight DTO specifically for logging in
    @lombok.Data
    public static class LoginRequest {
        private String email;
        private String password;
    }
}