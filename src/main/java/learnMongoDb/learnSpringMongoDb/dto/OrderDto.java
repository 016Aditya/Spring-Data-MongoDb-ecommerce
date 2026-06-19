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

    // ── Return request ────────────────────────────────────────────────────────

    @Data
    public static class ReturnRequest {
        /** Reason the customer is returning the order. */
        private String reason;

        /** ISO-8601 timestamp sent from the frontend. */
        private String requestedAt;
    }

    // ── Outbound ─────────────────────────────────────────────────────────────

    /**
     * Single order-item row returned inside every Order response.
     *
     * Both imageUrl and productImage are set to the same value.
     * The React frontend's normalizeOrderItem() checks imageUrl FIRST,
     * then productImage as fallback — so having both guarantees the image
     * is always resolved regardless of which field the normalizer hits.
     */
    @Data
    public static class OrderItemResponse {
        /** Original product document ID — kept for deep-links / analytics. */
        private String productId;

        /** Name snapshot — as it was when the order was placed. */
        private String productName;

        /**
         * Image URL — primary field checked by the frontend normalizer.
         * Always set to the absolute product image URL.
         */
        private String imageUrl;

        /**
         * Image URL — legacy / alias field for backward compatibility.
         * Set to the same value as imageUrl.
         */
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
         * The frontend reads items[0].productName, items[0].imageUrl, etc.
         */
        private List<OrderItemResponse> items;
    }

    // ── Admin / internal ─────────────────────────────────────────────────────

    @Data
    public static class UpdateStatusRequest {
        /** Target status, e.g. "SHIPPED", "DELIVERED", "CANCELLED", "RETURN_REQUESTED", "RETURNED". */
        private String status;
    }

    // ── Return status response ────────────────────────────────────────────────

    @Data
    public static class ReturnStatusResponse {
        private String orderId;
        private String status;
        private String message;
    }
}
