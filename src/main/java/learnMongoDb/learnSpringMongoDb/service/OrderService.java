package learnMongoDb.learnSpringMongoDb.service;

import learnMongoDb.learnSpringMongoDb.entity.Order;
import learnMongoDb.learnSpringMongoDb.entity.OrderItem;
import learnMongoDb.learnSpringMongoDb.entity.Product;
import learnMongoDb.learnSpringMongoDb.repository.OrderRepository;
import learnMongoDb.learnSpringMongoDb.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * OrderService — handles all business logic for orders.
 *
 * Core responsibility: build immutable OrderItem snapshots from live
 * Product documents so that order history is never affected by future
 * catalogue changes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    // ── Order creation ───────────────────────────────────────────────────────

    /**
     * Creates an order from a list of product IDs.
     *
     * For each ID:
     *   1. Fetches the live Product document from MongoDB.
     *   2. Builds an OrderItem snapshot (name, image, price — frozen at purchase time).
     *   3. Accumulates totalPrice and totalQuantity server-side.
     *
     * The resulting Order.items list is what the frontend renders.
     * No secondary product lookups are ever needed after this point.
     *
     * @param productIds  IDs of products to include (one unit each by default).
     * @param userId      Owning user's ID.
     * @param address     Delivery address from the request.
     * @return            Persisted Order with full item snapshots.
     */
    public Order createOrder(List<String> productIds, String userId,
                             learnMongoDb.learnSpringMongoDb.entity.Address address) {

        List<OrderItem> snapshots = new ArrayList<>();
        double grandTotal  = 0.0;
        int    totalQty    = 0;

        for (String productId : productIds) {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException(
                            "Product not found: " + productId));

            // Default quantity = 1 per product ID in this request.
            // To support qty > 1, extend Request DTO with a Map<String,Integer>.
            int qty       = 1;
            double unitPrice  = product.getPrice();
            double lineTotal  = unitPrice * qty;

            OrderItem snapshot = OrderItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .productImage(product.getImageUrl())
                    .price(unitPrice)
                    .quantity(qty)
                    .totalPrice(lineTotal)
                    .build();

            log.debug("Snapshot built — product='{}' image='{}' price={} qty={}",
                    product.getName(), product.getImageUrl(), unitPrice, qty);

            snapshots.add(snapshot);
            grandTotal += lineTotal;
            totalQty   += qty;
        }

        Order order = Order.builder()
                .userId(userId)
                .address(address)
                .items(snapshots)
                .quantity(totalQty)
                .totalPrice(grandTotal)
                .status("PENDING")
                .build();

        Order saved = orderRepository.save(order);
        log.info("Order created — id={} userId={} items={} total={}",
                saved.getId(), userId, snapshots.size(), grandTotal);
        return saved;
    }

    // ── Queries ──────────────────────────────────────────────────────────────

    public List<Order> getOrdersByUserId(String userId) {
        return orderRepository.findByUserId(userId);
    }

    public List<Order> getOrdersByStatusAndPrice(String status, double minPrice) {
        return orderRepository.findOrdersByStatusAndPrice(status, minPrice);
    }

    public List<Order> getOrdersByCity(String city) {
        return orderRepository.findByAddressCity(city);
    }

    public Order getOrderById(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
    }

    // ── Mutations ────────────────────────────────────────────────────────────

    public Order updateOrderStatus(String orderId, String newStatus) {
        Order order = getOrderById(orderId);
        order.setStatus(newStatus);
        Order saved = orderRepository.save(order);
        log.info("Order {} status updated to {}", orderId, newStatus);
        return saved;
    }

    /** Permanently removes the order document. Use with caution. */
    public void deleteOrder(String id) {
        orderRepository.deleteById(id);
        log.warn("Order {} permanently deleted", id);
    }
}
