package learnMongoDb.learnSpringMongoDb.controller;

import learnMongoDb.learnSpringMongoDb.dto.OrderDto;
import learnMongoDb.learnSpringMongoDb.entity.Order;
import learnMongoDb.learnSpringMongoDb.entity.OrderItem;
import learnMongoDb.learnSpringMongoDb.security.CustomUserDetails;
import learnMongoDb.learnSpringMongoDb.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * OrderController — REST API for orders.
 *
 * IDOR Fix (this commit)
 * ──────────────────────
 * The userId used to create or read orders is ALWAYS extracted from the
 * validated JWT via @AuthenticationPrincipal — never from the request body
 * or path variable supplied by the client.
 *
 * Before this fix, POST /api/orders accepted a userId in the request body.
 * An attacker could forge a different userId and place orders under another
 * user's account (IDOR — Insecure Direct Object Reference).
 *
 * After this fix:
 *   createOrder     — userId from JWT, request body userId is ignored
 *   getOrdersByUser — path userId must equal JWT userId (403 otherwise)
 *   getOrderById    — order.userId must equal JWT userId (403 otherwise)
 *   initiateReturn  — order.userId must equal JWT userId (403 otherwise)
 *
 * Admin-only endpoints (updateOrderStatus, getOrdersByCity, getOrdersByStatusAndPrice,
 * deleteOrder) should be further restricted with @PreAuthorize("hasRole('ADMIN')")
 * once method-level security is enabled (@EnableMethodSecurity).
 *
 * Backward-compat: legacy orders stored products under the "products"
 * MongoDB field. mapToResponse() falls back to order.getLegacyProducts()
 * when order.getItems() is empty so old documents render correctly
 * without any data migration.
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ── POST /api/orders ─────────────────────────────────────────────────────

    /**
     * Creates an order for the authenticated user.
     *
     * The userId is taken from the JWT principal — the userId field in the
     * request body (if present) is completely ignored to prevent IDOR.
     */
    @PostMapping
    public ResponseEntity<OrderDto.Response> createOrder(
            @RequestBody OrderDto.Request request,
            @AuthenticationPrincipal CustomUserDetails principal) {

        // ✅ userId from JWT — never from request.getUserId()
        String userId = principal.getUserId();

        Order saved = orderService.createOrder(
                request.getProductIds(),
                userId,               // trusted source
                request.getAddress());

        return ResponseEntity.ok(mapToResponse(saved));
    }

    // ── GET /api/orders/user/{userId} ────────────────────────────────────────

    /**
     * Lists orders for a user.
     *
     * The path variable is validated against the JWT userId.
     * A user can only fetch their own orders — not another user's.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getOrdersByUser(
            @PathVariable String userId,
            @AuthenticationPrincipal CustomUserDetails principal) {

        // ✅ IDOR guard — reject if the path userId doesn't match the JWT userId
        if (!principal.getUserId().equals(userId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }

        List<OrderDto.Response> responses = orderService.getOrdersByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    // ── GET /api/orders/{id} ─────────────────────────────────────────────────

    /**
     * Fetches a single order.
     *
     * Validates that the order belongs to the authenticated user.
     * Returns 403 if the order's userId doesn't match the JWT userId.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails principal) {

        Order order = orderService.getOrderById(id);

        // ✅ IDOR guard — reject if the order doesn't belong to this user
        if (!principal.getUserId().equals(order.getUserId())) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }

        return ResponseEntity.ok(mapToResponse(order));
    }

    // ── PATCH /api/orders/{id}/status ────────────────────────────────────────

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderDto.Response> updateOrderStatus(
            @PathVariable String id,
            @RequestBody OrderDto.UpdateStatusRequest request) {

        Order updated = orderService.updateOrderStatus(id, request.getStatus());
        return ResponseEntity.ok(mapToResponse(updated));
    }

    // ── POST /api/orders/{id}/return — customer initiates return ─────────────

    @PostMapping("/{id}/return")
    public ResponseEntity<?> initiateReturn(
            @PathVariable String id,
            @RequestBody(required = false) OrderDto.ReturnRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {

        Order order = orderService.getOrderById(id);

        // ✅ IDOR guard — only the order owner can request a return
        if (!principal.getUserId().equals(order.getUserId())) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }

        String reason = (request != null && request.getReason() != null)
                ? request.getReason()
                : "";

        Order updated = orderService.returnOrder(id, reason);
        return ResponseEntity.ok(mapToResponse(updated));
    }

    // ── GET /api/orders/{id}/return — fetch return status ────────────────────

    @GetMapping("/{id}/return")
    public ResponseEntity<?> getReturnStatus(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails principal) {

        Order order = orderService.getOrderById(id);

        // ✅ IDOR guard
        if (!principal.getUserId().equals(order.getUserId())) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }

        String status = order.getStatus();

        OrderDto.ReturnStatusResponse response = new OrderDto.ReturnStatusResponse();
        response.setOrderId(id);
        response.setStatus(status);

        boolean isReturnStatus = "RETURN_REQUESTED".equals(status) || "RETURNED".equals(status);
        response.setMessage(isReturnStatus
                ? "Return status: " + status
                : "No return initiated for this order");

        return ResponseEntity.ok(response);
    }

    // ── PATCH /api/orders/{id}/return — admin updates return status ───────────

    @PatchMapping("/{id}/return")
    public ResponseEntity<OrderDto.Response> updateReturnStatus(
            @PathVariable String id,
            @RequestBody OrderDto.UpdateStatusRequest request) {

        Order updated = orderService.updateOrderStatus(id, request.getStatus());
        return ResponseEntity.ok(mapToResponse(updated));
    }

    // ── DELETE /api/orders/{id}/return — cancel a return request ─────────────

    @DeleteMapping("/{id}/return")
    public ResponseEntity<?> cancelReturn(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails principal) {

        Order order = orderService.getOrderById(id);

        // ✅ IDOR guard
        if (!principal.getUserId().equals(order.getUserId())) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }

        if (!"RETURN_REQUESTED".equals(order.getStatus())) {
            return ResponseEntity.badRequest().build();
        }
        Order updated = orderService.updateOrderStatus(id, "DELIVERED");
        return ResponseEntity.ok(mapToResponse(updated));
    }

    // ── GET /api/orders/city/{city} ──────────────────────────────────────────

    @GetMapping("/city/{city}")
    public ResponseEntity<List<OrderDto.Response>> getOrdersByCity(
            @PathVariable String city) {

        List<OrderDto.Response> responses = orderService.getOrdersByCity(city)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    // ── GET /api/orders/status ───────────────────────────────────────────────

    @GetMapping("/status")
    public ResponseEntity<List<OrderDto.Response>> getOrdersByStatusAndPrice(
            @RequestParam String status,
            @RequestParam double minPrice) {

        List<OrderDto.Response> responses =
                orderService.getOrdersByStatusAndPrice(status, minPrice)
                        .stream()
                        .map(this::mapToResponse)
                        .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    // ── DELETE /api/orders/{id} ──────────────────────────────────────────────

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable String id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    // ── Mapping helper ───────────────────────────────────────────────────────

    private OrderDto.Response mapToResponse(Order order) {
        OrderDto.Response response = new OrderDto.Response();
        response.setId(order.getId());
        response.setUserId(order.getUserId());
        response.setQuantity(order.getQuantity());
        response.setTotalPrice(order.getTotalPrice());
        response.setStatus(order.getStatus());
        response.setAddress(order.getAddress());
        response.setCreatedAt(order.getCreatedAt());

        List<OrderItem> rawItems = order.getItems();
        if (rawItems == null || rawItems.isEmpty()) {
            rawItems = order.getLegacyProducts();
        }
        if (rawItems == null) {
            rawItems = Collections.emptyList();
        }

        List<OrderDto.OrderItemResponse> itemResponses = rawItems.stream()
                .map(item -> {
                    OrderDto.OrderItemResponse ir = new OrderDto.OrderItemResponse();
                    ir.setProductId(item.getProductId());
                    ir.setProductName(item.getProductName());
                    String imageUrl = item.getProductImage();
                    ir.setImageUrl(imageUrl);
                    ir.setProductImage(imageUrl);
                    ir.setPrice(item.getPrice());
                    ir.setQuantity(item.getQuantity());
                    ir.setTotalPrice(item.getTotalPrice());
                    return ir;
                })
                .collect(Collectors.toList());

        response.setItems(itemResponses);
        return response;
    }
}
