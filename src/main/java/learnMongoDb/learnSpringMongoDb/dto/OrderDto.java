package learnMongoDb.learnSpringMongoDb.dto;

import learnMongoDb.learnSpringMongoDb.entity.Address;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDto {

    @Data
    public static class Request {
        private String userId;
        private Integer quantity;
        private Address address;
        private List<String> productIds; // Just send IDs, not the whole product objects!
    }

    @Data
    public static class Response {
        private String id;
        private String userId;
        private Integer quantity;
        private Double totalPrice;
        private String status;
        private Address address;
        private LocalDateTime createdAt;
    }

    @Data
    public static class UpdateStatusRequest {
        private String status; // e.g., "SHIPPED", "DELIVERED", "CANCELLED"
    }
}