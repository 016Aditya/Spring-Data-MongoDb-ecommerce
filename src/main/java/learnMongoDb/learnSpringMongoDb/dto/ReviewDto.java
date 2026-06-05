package learnMongoDb.learnSpringMongoDb.dto;

import lombok.Data;
import java.time.LocalDateTime;

public class ReviewDto {

    @Data
    public static class Request {
        private String productId;
        private String userId;
        private Integer rating;
        private String comment;
    }

    // NEW: DTO for searching reviews via a POST body
    @Data
    public static class ProductSearchRequest {
        private String productId;
    }

    @Data
    public static class Response {
        private String id;
        private String productId;
        private String userId;
        private Integer rating;
        private String comment;
        private LocalDateTime createdAt;
    }

    @Data
    public static class UpdateRequest {
        private Integer rating;
        private String comment;
    }
}