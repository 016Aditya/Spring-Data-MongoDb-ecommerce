package learnMongoDb.learnSpringMongoDb.service;

import learnMongoDb.learnSpringMongoDb.entity.Order;
import learnMongoDb.learnSpringMongoDb.entity.OrderItem;
import learnMongoDb.learnSpringMongoDb.entity.Product;
import learnMongoDb.learnSpringMongoDb.repository.OrderRepository;
import learnMongoDb.learnSpringMongoDb.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    // ── Order creation ──────────────────────────────────────────────────

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

            int qty          = 1;
            double unitPrice = product.getPrice();
            double lineTotal = unitPrice * qty;

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

    // ── Queries ──────────────────────────────────────────────────────

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

    // ── Mutations ──────────────────────────────────────────────────

    public Order updateOrderStatus(String orderId, String newStatus) {
        Order order = getOrderById(orderId);
        order.setStatus(newStatus);
        Order saved = orderRepository.save(order);
        log.info("Order {} status updated to {}", orderId, newStatus);
        return saved;
    }

    /**
     * Customer initiates a return for a delivered order.
     *
     * Business rule: only DELIVERED orders can be returned.
     *
     * Sets:
     *   status            → RETURN_REQUESTED
     *   returnRequestedAt → now()
     *   refundStatus      → "PENDING"
     *
     * @param orderId  The order to return.
     * @param reason   Customer-supplied return reason (may be empty string).
     * @return         Updated order with return fields populated.
     */
    public Order returnOrder(String orderId, String reason) {
        Order order = getOrderById(orderId);

        String currentStatus = order.getStatus();
        if (!"DELIVERED".equalsIgnoreCase(currentStatus)) {
            throw new IllegalStateException(
                    "Only DELIVERED orders can be returned. Current status: " + currentStatus);
        }

        order.setStatus("RETURN_REQUESTED");
        order.setReturnRequestedAt(LocalDateTime.now());
        order.setRefundStatus("PENDING");

        Order saved = orderRepository.save(order);
        log.info("Return requested — orderId={} reason='{}'", orderId, reason);
        return saved;
    }

    // ── Admin / system return lifecycle methods ────────────────────────────

    /**
     * Admin: approve the return request.
     * Transitions: RETURN_REQUESTED → RETURN_APPROVED
     */
    public Order approveReturn(String orderId) {
        return transitionReturnStatus(orderId, "RETURN_REQUESTED", "RETURN_APPROVED");
    }

    /**
     * Admin: schedule pickup for the returned item.
     * Transitions: RETURN_APPROVED → PICKUP_SCHEDULED
     */
    public Order schedulePickup(String orderId) {
        return transitionReturnStatus(orderId, "RETURN_APPROVED", "PICKUP_SCHEDULED");
    }

    /**
     * Admin: mark item as picked up from the customer.
     * Transitions: PICKUP_SCHEDULED → PICKED_UP
     */
    public Order markPickedUp(String orderId) {
        return transitionReturnStatus(orderId, "PICKUP_SCHEDULED", "PICKED_UP");
    }

    /**
     * Admin: mark refund as processed.
     * Transitions: PICKED_UP → REFUND_PROCESSED
     * Also sets refundStatus = "PROCESSED".
     */
    public Order processRefund(String orderId) {
        Order order = getOrderById(orderId);
        if (!"PICKED_UP".equalsIgnoreCase(order.getStatus())) {
            throw new IllegalStateException(
                    "Expected PICKED_UP but found: " + order.getStatus());
        }
        order.setStatus("REFUND_PROCESSED");
        order.setRefundStatus("PROCESSED");
        Order saved = orderRepository.save(order);
        log.info("Refund processed — orderId={}", orderId);
        return saved;
    }

    /**
     * Admin: complete the return lifecycle.
     * Transitions: REFUND_PROCESSED → RETURN_SUCCESSFUL
     * Also sets returnCompletedAt = now().
     */
    public Order completeReturn(String orderId) {
        Order order = getOrderById(orderId);
        if (!"REFUND_PROCESSED".equalsIgnoreCase(order.getStatus())) {
            throw new IllegalStateException(
                    "Expected REFUND_PROCESSED but found: " + order.getStatus());
        }
        order.setStatus("RETURN_SUCCESSFUL");
        order.setReturnCompletedAt(LocalDateTime.now());
        Order saved = orderRepository.save(order);
        log.info("Return completed — orderId={}", orderId);
        return saved;
    }

    /** Permanently removes the order document. Use with caution. */
    public void deleteOrder(String id) {
        orderRepository.deleteById(id);
        log.warn("Order {} permanently deleted", id);
    }

    // ── Private helpers ───────────────────────────────────────────────

    private Order transitionReturnStatus(String orderId, String expectedCurrent, String next) {
        Order order = getOrderById(orderId);
        if (!expectedCurrent.equalsIgnoreCase(order.getStatus())) {
            throw new IllegalStateException(
                    "Expected " + expectedCurrent + " but found: " + order.getStatus());
        }
        order.setStatus(next);
        Order saved = orderRepository.save(order);
        log.info("Return status transition — orderId={} {} → {}", orderId, expectedCurrent, next);
        return saved;
    }
}
