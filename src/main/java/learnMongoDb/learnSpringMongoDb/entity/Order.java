package learnMongoDb.learnSpringMongoDb.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order entity — root aggregate stored in the "orders" MongoDB collection.
 *
 * Key design decision:
 * --------------------
 * Items are stored as embedded OrderItem documents (product snapshots),
 * NOT as @DBRef references to the Product collection.
 *
 * Why embedded snapshots instead of @DBRef?
 *   - @DBRef stores only the product _id and resolves at read time.
 *     If a product is deleted or its price/image changes, old order
 *     history silently breaks.
 *   - Embedded snapshots are immutable. The order always reflects
 *     exactly what the customer purchased at the time they bought it.
 *   - Eliminates N+1 query problem: no secondary lookups needed to
 *     render the Orders page or Order Detail page.
 *
 * Backward-compat note:
 * ---------------------
 * Orders created before the snapshot migration stored products under
 * the field name "products" in MongoDB. The @Field("products") alias
 * on the `legacyProducts` field reads those old documents without any
 * data migration. mapToResponse() in OrderController prefers `items`
 * and falls back to `legacyProducts` so both old and new documents work.
 *
 * Return fields (additive — nullable):
 * -------------------------------------
 * returnRequestedAt, returnCompletedAt, refundStatus are all nullable.
 * Old MongoDB documents without these fields deserialize safely to null.
 * No data migration required.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "orders")
@CompoundIndex(name = "idx_quantity_status", def = "{'quantity':-1,'status':1}")
@CompoundIndex(name = "idx_address_city",    def = "{'address.city': -1}")
public class Order {

    @Id
    private String id;

    @Indexed
    private String userId;

    /**
     * Total number of units across all items.
     * Computed by OrderService — not set by the client.
     */
    private Integer quantity;

    /**
     * Grand total in INR.
     * Computed server-side from item snapshots — never trusted from the client.
     */
    private Double totalPrice;

    @Indexed
    private String status;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private Address address;

    /**
     * Embedded product snapshots — one entry per distinct product.
     * New orders (post-migration) store snapshots here under MongoDB field "items".
     */
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    /**
     * BACKWARD-COMPAT: legacy documents created before the snapshot migration
     * stored products under the MongoDB field name "products".
     *
     * Spring Data reads the "products" array from MongoDB into this field.
     * OrderController.mapToResponse() checks items first, then falls back here.
     *
     * Do NOT write to this field — new orders always use `items`.
     */
    @Field("products")
    @Builder.Default
    private List<OrderItem> legacyProducts = new ArrayList<>();

    // ── Return fields (additive — all nullable) ──────────────────────────────

    /**
     * Timestamp when the customer requested the return.
     * Null for orders that have never entered the return flow.
     * Set by OrderService.returnOrder() when status → RETURN_REQUESTED.
     */
    private LocalDateTime returnRequestedAt;

    /**
     * Timestamp when the return was fully completed (status = RETURN_SUCCESSFUL).
     * Null until the return lifecycle finishes.
     */
    private LocalDateTime returnCompletedAt;

    /**
     * Refund lifecycle state: "PENDING" | "PROCESSED" | null.
     * Null for orders that have never been returned.
     * Set to "PENDING" when return is initiated; "PROCESSED" when refund completes.
     */
    private String refundStatus;
}
