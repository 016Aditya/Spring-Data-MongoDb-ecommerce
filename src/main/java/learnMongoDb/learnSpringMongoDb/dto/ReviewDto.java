package learnMongoDb.learnSpringMongoDb.dto;

import lombok.Data;
import java.time.LocalDateTime;

public class ReviewDto {

    @Data
    public static class Request {
        private String productId;
        private Integer rating;
        private String comment;
        // userId is intentionally omitted for security!
    }

    @Data
    public static class Response {
        private String id;
        private String productId;
        private String userId;
        private String userName;
        private String userEmail;
        private Integer rating;
        private String comment;
        private LocalDateTime createdAt;
    }

    @Data
    public static class UpdateRequest {
        private Integer rating;
        private String comment;
    }

    @Data
    public static class ProductSearchRequest {
        private String productId;
    }
}