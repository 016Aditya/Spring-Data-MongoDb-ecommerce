package learnMongoDb.learnSpringMongoDb.dto;

import lombok.Data;
import java.time.LocalDateTime;

public class UserDto {

    @Data
    public static class Request {
        private String firstName;
        private String lastName;
        private String email;
        private String password;
    }

    @Data
    public static class Response {
        private String id;
        private String firstName;
        private String lastName;
        private String email;
        private String role;
        private LocalDateTime createdAt;
    }

    @Data
    public static class UpdateProfileRequest {
        private String firstName;
        private String lastName;
        private String password;
    }
}