package learnMongoDb.learnSpringMongoDb.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

public class CartDto {

    // ── Normal inbound requests ────────────────────────────────────────

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

    // ── Sync inbound request ──────────────────────────────────────────

    /**
     * Request body for POST /api/cart/{userId}/sync
     *
     * The frontend sends the guest LocalStorage cart after login.
     * Each entry carries a productId and the quantity the guest selected.
     *
     * Example JSON:
     * {
     *   "items": [
     *     { "productId": "64b123", "quantity": 2 },
     *     { "productId": "64c888", "quantity": 1 }
     *   ]
     * }
     */
    @Data
    public static class SyncRequest {

        @NotNull(message = "items list must not be null")
        @Valid
        private List<CartItemRequest> items;
    }

    @Data
    public static class CartItemRequest {

        @NotBlank(message = "productId must not be blank")
        private String productId;

        @NotNull(message = "quantity must not be null")
        @Positive(message = "quantity must be positive")
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