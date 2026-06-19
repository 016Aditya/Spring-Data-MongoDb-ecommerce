package learnMongoDb.learnSpringMongoDb.dto;

import learnMongoDb.learnSpringMongoDb.entity.Address;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * OrderDto — request / response shapes for the Orders API.
 *
 * Response.items now contains full product snapshots so the frontend
 * can render order cards and order detail pages without any extra
 * product lookups.
 */
public class OrderDto {

    // ── Inbound ─────────────────────────────────────────────────────────────

    @Data
    public static class Request {
        private String userId;

        /**
         * List of product IDs to order.
         * Quantity per product defaults to 1 unless overridden by
         * productQuantities map (future extension).
         */
        private List<String> productIds;

        private Address address;
    }

    // ── Outbound ─────────────────────────────────────────────────────────────

    /**
     * Single order-item row returned inside every Order response.
     * Fields mirror what the React frontend's normalizeOrderItem() expects.
     */
    @Data
    public static class OrderItemResponse {
        /** Original product document ID — kept for deep-links / analytics. */
        private String productId;

        /** Name snapshot — as it was when the order was placed. */
        private String productName;

        /** Image URL snapshot. */
        private String productImage;

        /** Unit price at purchase time. */
        private Double price;

        /** Quantity purchased. */
        private Integer quantity;

        /** price * quantity — pre-computed line total. */
        private Double totalPrice;
    }

    @Data
    public static class Response {
        private String id;
        private String userId;

        /** Total units across all items. */
        private Integer quantity;

        /** Grand total in INR. */
        private Double totalPrice;

        private String status;
        private Address address;
        private LocalDateTime createdAt;

        /**
         * Product snapshot list — always populated.
         * The frontend reads items[0].productName, items[0].productImage, etc.
         */
        private List<OrderItemResponse> items;
    }

    // ── Admin / internal ─────────────────────────────────────────────────────

    @Data
    public static class UpdateStatusRequest {
        /** Target status, e.g. "SHIPPED", "DELIVERED", "CANCELLED". */
        private String status;
    }
}
