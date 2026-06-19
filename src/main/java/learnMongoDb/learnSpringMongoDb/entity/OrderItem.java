package learnMongoDb.learnSpringMongoDb.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * OrderItem — embedded product snapshot stored inside an Order document.
 *
 * Design rationale:
 * -----------------
 * We copy product data (name, image, price) at the time of order creation.
 * This means:
 *   1. The order history is immutable — editing/deleting a Product never corrupts past orders.
 *   2. Every API response includes full item details with zero extra DB lookups.
 *   3. The frontend needs no secondary product-fetch to render the orders page.
 *
 * This is the standard e-commerce pattern (Shopify, Amazon, etc. all use snapshots).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    /** Reference to the original Product document (kept for analytics / admin). */
    private String productId;

    /** Snapshot of the product name at order time. */
    private String productName;

    /** Snapshot of the product image URL at order time. */
    private String productImage;

    /** Unit price at the time of purchase (NOT the current catalogue price). */
    private Double price;

    /** Number of units purchased. */
    private Integer quantity;

    /** price * quantity — pre-computed for convenience. */
    private Double totalPrice;
}
