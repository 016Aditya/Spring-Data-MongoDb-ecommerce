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
     * Embedded product snapshots — one entry per distinct product in the order.
     * Replaces the old @DBRef List<Product> approach.
     */
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();
}
