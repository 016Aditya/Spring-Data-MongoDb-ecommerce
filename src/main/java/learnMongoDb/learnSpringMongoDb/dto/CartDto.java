package learnMongoDb.learnSpringMongoDb.dto;

import lombok.Data;

public class CartDto {

    @Data
    public static class AddItemRequest {
        private String productId;
        private Integer quantity;
    }
}