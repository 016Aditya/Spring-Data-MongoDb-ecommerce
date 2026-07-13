package learnMongoDb.learnSpringMongoDb.service;

import learnMongoDb.learnSpringMongoDb.entity.*;
import learnMongoDb.learnSpringMongoDb.error.ResourceNotFoundException;
import learnMongoDb.learnSpringMongoDb.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * OrderService — Commit 2: orchestrator for the checkout workflow.
 * Does NOT touch stock directly — all inventory via InventoryService.
 *
 * Two-Phase Checkout:
 *   Phase A — CartValidator validates ALL items (no stock changes)
 *   Phase B — InventoryService.decreaseStockBatch → save order →
 *             clearPurchasedCartItems
 *   Compensation — restoreStock if Phase B fails after stock deduction
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository        orderRepository;
    private final ProductRepository      productRepository;
    private final ShoppingCartRepository cartRepository;
    private final CartValidator          cartValidator;
    private final InventoryService       inventoryService;

    // ── Main checkout entry point ──────────────────────────────────────
    public Order checkout(String userId, Address address,
                          Map<String, Integer> productQuantities) {

        // PHASE A — validate ALL items, zero stock changes
        log.info("Phase A: validating {} cart items for userId={}",
                productQuantities.size(), userId);
        for (Map.Entry<String, Integer> entry : productQuantities.entrySet()) {
            cartValidator.validateAndGet(entry.getKey(), entry.getValue());
        }
        log.info("Phase A passed — entering Phase B for userId={}", userId);

        // PHASE B — deduct + save + clear cart
        Map<String, Integer> previousStocks = null;
        try {
            previousStocks = inventoryService.decreaseStockBatch(productQuantities);

            List<OrderItem> snapshots = buildSnapshots(productQuantities);
            double grandTotal = snapshots.stream()
                    .mapToDouble(OrderItem::getTotalPrice).sum();
            int totalQty = snapshots.stream()
                    .mapToInt(OrderItem::getQuantity).sum();

            Order order = Order.builder()
                    .userId(userId).address(address).items(snapshots)
                    .quantity(totalQty).totalPrice(grandTotal)
                    .status("PENDING").build();

            Order saved = orderRepository.save(order);
            log.info("Order saved — id={} userId={} items={} total={}",
                    saved.getId(), userId, snapshots.size(), grandTotal);

            clearPurchasedCartItems(userId, productQuantities.keySet());
            return saved;

        } catch (Exception ex) {
            // Compensation: restore stock if anything in Phase B fails
            if (previousStocks != null) {
                log.error("Phase B failed — restoring stock. Reason: {}", ex.getMessage());
                inventoryService.restoreStock(previousStocks);
            }
            throw ex;
        }
    }

    // ── Backward-compat createOrder (delegates to checkout) ───────────
    public Order createOrder(List<String> productIds, String userId,
                             Address address, Map<String, Integer> productQuantities) {
        Map<String, Integer> quantities = new HashMap<>();
        for (String productId : productIds) {
            quantities.put(productId,
                    (productQuantities != null && productQuantities.containsKey(productId))
                            ? Math.max(1, productQuantities.get(productId)) : 1);
        }
        return checkout(userId, address, quantities);
    }

    // ── Queries (unchanged) ────────────────────────────────────────────
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
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
    }

    // ── Mutations (unchanged) ──────────────────────────────────────────
    public Order updateOrderStatus(String orderId, String newStatus) {
        Order order = getOrderById(orderId);
        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    public Order returnOrder(String orderId, String reason) {
        Order order = getOrderById(orderId);
        if (!"DELIVERED".equalsIgnoreCase(order.getStatus()))
            throw new IllegalStateException(
                    "Only DELIVERED orders can be returned. Current: " + order.getStatus());
        order.setStatus("RETURN_REQUESTED");
        order.setReturnRequestedAt(LocalDateTime.now());
        order.setRefundStatus("PENDING");
        return orderRepository.save(order);
    }

    public Order approveReturn(String orderId) {
        return transitionReturnStatus(orderId, "RETURN_REQUESTED", "RETURN_APPROVED");
    }
    public Order schedulePickup(String orderId) {
        return transitionReturnStatus(orderId, "RETURN_APPROVED", "PICKUP_SCHEDULED");
    }
    public Order markPickedUp(String orderId) {
        return transitionReturnStatus(orderId, "PICKUP_SCHEDULED", "PICKED_UP");
    }
    public Order processRefund(String orderId) {
        Order order = getOrderById(orderId);
        if (!"PICKED_UP".equalsIgnoreCase(order.getStatus()))
            throw new IllegalStateException("Expected PICKED_UP but found: " + order.getStatus());
        order.setStatus("REFUND_PROCESSED");
        order.setRefundStatus("PROCESSED");
        return orderRepository.save(order);
    }
    public Order completeReturn(String orderId) {
        Order order = getOrderById(orderId);
        if (!"REFUND_PROCESSED".equalsIgnoreCase(order.getStatus()))
            throw new IllegalStateException("Expected REFUND_PROCESSED but found: " + order.getStatus());
        order.setStatus("RETURN_SUCCESSFUL");
        order.setReturnCompletedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }
    public void deleteOrder(String id) {
        orderRepository.deleteById(id);
    }

    // ── Private helpers ────────────────────────────────────────────────
    private List<OrderItem> buildSnapshots(Map<String, Integer> productQuantities) {
        List<OrderItem> snapshots = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : productQuantities.entrySet()) {
            Product product = productRepository.findById(entry.getKey())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found during snapshot: " + entry.getKey()));
            double unitPrice = product.getPrice();
            snapshots.add(OrderItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .productImage(product.getImageUrl())
                    .price(unitPrice).quantity(entry.getValue())
                    .totalPrice(unitPrice * entry.getValue()).build());
        }
        return snapshots;
    }

    private void clearPurchasedCartItems(String userId,
                                         Set<String> purchasedProductIds) {
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            if (cart.getItems() == null) return;
            boolean anyRemoved = cart.getItems()
                    .removeIf(item -> purchasedProductIds.contains(item.getProductId()));
            if (anyRemoved) {
                double newTotal = cart.getItems().stream()
                        .mapToDouble(i -> i.getUnitPrice() * i.getQuantity()).sum();
                cart.setCartTotal(newTotal);
                cartRepository.save(cart);
            }
        });
    }

    private Order transitionReturnStatus(String orderId,
                                         String expectedCurrent, String next) {
        Order order = getOrderById(orderId);
        if (!expectedCurrent.equalsIgnoreCase(order.getStatus()))
            throw new IllegalStateException(
                    "Expected " + expectedCurrent + " but found: " + order.getStatus());
        order.setStatus(next);
        return orderRepository.save(order);
    }
}