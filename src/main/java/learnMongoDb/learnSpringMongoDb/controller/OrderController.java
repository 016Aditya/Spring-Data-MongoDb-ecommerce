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
 * IDOR Fix:
 * ──────────
 * The userId used to create or read orders is ALWAYS extracted from the
 * validated JWT via @AuthenticationPrincipal — never from the request body
 * or path variable supplied by the client.
 *
 * Return endpoint fix:
 * ───────────────────
 * Customer return initiation is now PATCH /{id}/return (was @PostMapping — caused
 * HTTP 405 when frontend sent PATCH, and conflicted with the admin PATCH handler).
 *
 * Admin return status updates moved to PATCH /{id}/return/status to eliminate
 * the mapping collision.
 *
 * mapToResponse() now maps returnRequestedAt, returnCompletedAt, refundStatus
 * so the frontend receives these fields on every order response.
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ── POST /api/orders ─────────────────────────────────────────────────

    /**
     * Creates an order for the authenticated user.
     * userId is taken from the JWT — never from the request body (IDOR prevention).
     */
    @PostMapping
    public ResponseEntity<OrderDto.Response> createOrder(
            @RequestBody OrderDto.Request request,
            @AuthenticationPrincipal CustomUserDetails principal) {

        String userId = principal.getUserId();

        Order saved = orderService.createOrder(
                request.getProductIds(),
                userId,
                request.getAddress());

        return ResponseEntity.ok(mapToResponse(saved));
    }

    // ── GET /api/orders/user/{userId} ───────────────────────────────────────

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getOrdersByUser(
            @PathVariable String userId,
            @AuthenticationPrincipal CustomUserDetails principal) {

        if (!principal.getUserId().equals(userId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }

        List<OrderDto.Response> responses = orderService.getOrdersByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    // ── GET /api/orders/{id} ─────────────────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails principal) {

        Order order = orderService.getOrderById(id);

        if (!principal.getUserId().equals(order.getUserId())) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }

        return ResponseEntity.ok(mapToResponse(order));
    }

    // ── PATCH /api/orders/{id}/status ───────────────────────────────────────

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderDto.Response> updateOrderStatus(
            @PathVariable String id,
            @RequestBody OrderDto.UpdateStatusRequest request) {

        Order updated = orderService.updateOrderStatus(id, request.getStatus());
        return ResponseEntity.ok(mapToResponse(updated));
    }

    // ── PATCH /api/orders/{id}/return — customer initiates return ────────────

    /**
     * FIX: Changed from @PostMapping to @PatchMapping.
     *
     * The frontend returnService.js sends PATCH /api/orders/{id}/return.
     * Previously this was @PostMapping which caused:
     *   1. HTTP 405 Method Not Allowed (POST vs PATCH mismatch)
     *   2. When PATCH did match, it hit the admin updateReturnStatus handler
     *      which expected a {"status":"..."} body — not a return initiation.
     *
     * Now: PATCH /{id}/return  →  customer return initiation (this method)
     *      PATCH /{id}/return/status  →  admin status update (see below)
     */
    @PatchMapping("/{id}/return")
    public ResponseEntity<?> initiateReturn(
            @PathVariable String id,
            @RequestBody(required = false) OrderDto.ReturnRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {

        Order order = orderService.getOrderById(id);

        // IDOR guard — only the order owner can request a return
        if (!principal.getUserId().equals(order.getUserId())) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }

        String reason = (request != null && request.getReason() != null)
                ? request.getReason()
                : "";

        Order updated = orderService.returnOrder(id, reason);
        return ResponseEntity.ok(mapToResponse(updated));
    }

    // ── GET /api/orders/{id}/return — fetch return status ──────────────────

    @GetMapping("/{id}/return")
    public ResponseEntity<?> getReturnStatus(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails principal) {

        Order order = orderService.getOrderById(id);

        if (!principal.getUserId().equals(order.getUserId())) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }

        String status = order.getStatus();

        OrderDto.ReturnStatusResponse response = new OrderDto.ReturnStatusResponse();
        response.setOrderId(id);
        response.setStatus(status);

        boolean isReturnStatus = status != null && status.startsWith("RETURN");
        response.setMessage(isReturnStatus
                ? "Return status: " + status
                : "No return initiated for this order");

        return ResponseEntity.ok(response);
    }

    // ── PATCH /api/orders/{id}/return/status — admin updates return status ──

    /**
     * Admin endpoint to advance the return lifecycle.
     * Moved from PATCH /{id}/return to PATCH /{id}/return/status
     * to avoid collision with the customer-facing PATCH /{id}/return.
     */
    @PatchMapping("/{id}/return/status")
    public ResponseEntity<OrderDto.Response> updateReturnStatus(
            @PathVariable String id,
            @RequestBody OrderDto.UpdateStatusRequest request) {

        Order updated = orderService.updateOrderStatus(id, request.getStatus());
        return ResponseEntity.ok(mapToResponse(updated));
    }

    // ── DELETE /api/orders/{id}/return — cancel a return request ───────────

    @DeleteMapping("/{id}/return")
    public ResponseEntity<?> cancelReturn(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails principal) {

        Order order = orderService.getOrderById(id);

        if (!principal.getUserId().equals(order.getUserId())) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }

        if (!"RETURN_REQUESTED".equals(order.getStatus())) {
            return ResponseEntity.badRequest().build();
        }
        Order updated = orderService.updateOrderStatus(id, "DELIVERED");
        return ResponseEntity.ok(mapToResponse(updated));
    }

    // ── GET /api/orders/city/{city} ────────────────────────────────────────

    @GetMapping("/city/{city}")
    public ResponseEntity<List<OrderDto.Response>> getOrdersByCity(
            @PathVariable String city) {

        List<OrderDto.Response> responses = orderService.getOrdersByCity(city)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    // ── GET /api/orders/status ─────────────────────────────────────────────

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

    // ── DELETE /api/orders/{id} ────────────────────────────────────────────

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable String id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    // ── Mapping helper ────────────────────────────────────────────────

    private OrderDto.Response mapToResponse(Order order) {
        OrderDto.Response response = new OrderDto.Response();
        response.setId(order.getId());
        response.setUserId(order.getUserId());
        response.setQuantity(order.getQuantity());
        response.setTotalPrice(order.getTotalPrice());
        response.setStatus(order.getStatus());
        response.setAddress(order.getAddress());
        response.setCreatedAt(order.getCreatedAt());

        // FIX: map return fields so frontend receives them on every response
        response.setReturnRequestedAt(order.getReturnRequestedAt());
        response.setReturnCompletedAt(order.getReturnCompletedAt());
        response.setRefundStatus(order.getRefundStatus());

        // Backward-compat: prefer items[], fall back to legacy products[]
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
