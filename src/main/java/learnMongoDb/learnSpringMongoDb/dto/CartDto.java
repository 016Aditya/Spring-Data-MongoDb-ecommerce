package learnMongoDb.learnSpringMongoDb.dto;

import lombok.Data;
import java.util.List;

public class CartDto {

    // ── Inbound ────────────────────────────────────────────────────────

    @Data
    public static class AddItemRequest {
        private String productId;
        private Integer quantity;
    }

    @Data
    public static class UpdateItemRequest {
        private String productId;
        private Integer quantity;
    }

    // ── Outbound ───────────────────────────────────────────────────────

    @Data
    public static class Response {
        private String id;
        private String userId;
        private Double cartTotal;
        private List<CartItemResponse> items;
    }

    @Data
    public static class CartItemResponse {
        private String productId;
        private Integer quantity;
        private Double unitPrice;
    }
}