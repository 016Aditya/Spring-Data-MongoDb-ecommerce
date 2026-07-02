package learnMongoDb.learnSpringMongoDb.controller;

import learnMongoDb.learnSpringMongoDb.dto.OrderDto;
import learnMongoDb.learnSpringMongoDb.entity.Order;
import learnMongoDb.learnSpringMongoDb.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * OrderController — REST endpoints for the Orders domain.
 *
 * Base path : /api/orders
 *
 * Customer endpoints
 *   POST   /api/orders                          → create order
 *   GET    /api/orders/user/{userId}            → order history for a user
 *   GET    /api/orders/{orderId}                → single order detail
 *   PUT    /api/orders/{orderId}/cancel         → cancel a PENDING order
 *   POST   /api/orders/{orderId}/return         → initiate return for a DELIVERED order
 *
 * Admin / system endpoints
 *   PUT    /api/orders/{orderId}/status         → update order status
 *   PUT    /api/orders/{orderId}/return/approve → approve return request
 *   PUT    /api/orders/{orderId}/return/pickup  → schedule pickup
 *   PUT    /api/orders/{orderId}/return/picked  → mark item picked up
 *   PUT    /api/orders/{orderId}/return/refund  → process refund
 *   PUT    /api/orders/{orderId}/return/complete→ complete return lifecycle
 *   DELETE /api/orders/{orderId}                → hard delete (admin only)
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService  orderService;
    private final ModelMapper   modelMapper;

    // ── CREATE ───────────────────────────────────────────────────────────────

    /**
     * POST /api/orders
     * Body: {
     *   userId,
     *   productIds: ["..."],
     *   productQuantities: { "productId": qty },   ← new optional field
     *   address: { street, city, state, zipCode, country }
     * }
     *
     * productQuantities is optional — if omitted every item defaults to qty=1.
     * The frontend always sends it so cart quantities are faithfully stored.
     *
     * Returns 201 + created order.
     */
    @PostMapping
    public ResponseEntity<OrderDto.Response> createOrder(
            @RequestBody OrderDto.Request request) {

        Order order = orderService.createOrder(
                request.getProductIds(),
                request.getUserId(),
                request.getAddress(),
                request.getProductQuantities()   // pass quantity map to service
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(order));
    }

    // ── READ ────────────────────────────────────────────────────────────────

    /**
     * GET /api/orders/user/{userId}
     * Returns the full order history for the given user.
     * NOTE: path must be declared before /{orderId} so Spring
     * does not treat "user" as an orderId wildcard.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderDto.Response>> getOrdersByUser(
            @PathVariable String userId) {

        List<OrderDto.Response> responses = orderService.getOrdersByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * GET /api/orders/{orderId}
     * Returns a single order document.
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDto.Response> getOrderById(
            @PathVariable String orderId) {

        Order order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(mapToResponse(order));
    }

    // ── CANCEL ─────────────────────────────────────────────────────────────

    /**
     * PUT /api/orders/{orderId}/cancel
     * Customer-facing cancel. Only PENDING orders can be cancelled.
     * Sets status → CANCELLED.
     */
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderDto.Response> cancelOrder(
            @PathVariable String orderId) {

        Order order = orderService.getOrderById(orderId);
        if (!"PENDING".equalsIgnoreCase(order.getStatus())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .build();
        }
        Order cancelled = orderService.updateOrderStatus(orderId, "CANCELLED");
        return ResponseEntity.ok(mapToResponse(cancelled));
    }

    // ── RETURN LIFECYCLE ─────────────────────────────────────────────────────

    /**
     * POST /api/orders/{orderId}/return
     * Customer initiates a return on a DELIVERED order.
     * Body: { reason: "string", requestedAt: "ISO-8601" }
     */
    @PostMapping("/{orderId}/return")
    public ResponseEntity<OrderDto.Response> initiateReturn(
            @PathVariable String orderId,
            @RequestBody OrderDto.ReturnRequest returnRequest) {

        Order returned = orderService.returnOrder(orderId, returnRequest.getReason());
        return ResponseEntity.ok(mapToResponse(returned));
    }

    // ── ADMIN STATUS UPDATE ──────────────────────────────────────────────────

    /**
     * PUT /api/orders/{orderId}/status
     * Admin: free-form status update.
     * Body: { "status": "SHIPPED" }
     */
    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderDto.Response> updateStatus(
            @PathVariable String orderId,
            @RequestBody OrderDto.UpdateStatusRequest body) {

        Order updated = orderService.updateOrderStatus(orderId, body.getStatus());
        return ResponseEntity.ok(mapToResponse(updated));
    }

    // ── ADMIN RETURN LIFECYCLE TRANSITIONS ─────────────────────────────────

    /** PUT /api/orders/{orderId}/return/approve */
    @PutMapping("/{orderId}/return/approve")
    public ResponseEntity<OrderDto.ReturnStatusResponse> approveReturn(
            @PathVariable String orderId) {

        Order order = orderService.approveReturn(orderId);
        return ResponseEntity.ok(buildReturnStatus(order, "Return approved"));
    }

    /** PUT /api/orders/{orderId}/return/pickup */
    @PutMapping("/{orderId}/return/pickup")
    public ResponseEntity<OrderDto.ReturnStatusResponse> schedulePickup(
            @PathVariable String orderId) {

        Order order = orderService.schedulePickup(orderId);
        return ResponseEntity.ok(buildReturnStatus(order, "Pickup scheduled"));
    }

    /** PUT /api/orders/{orderId}/return/picked */
    @PutMapping("/{orderId}/return/picked")
    public ResponseEntity<OrderDto.ReturnStatusResponse> markPickedUp(
            @PathVariable String orderId) {

        Order order = orderService.markPickedUp(orderId);
        return ResponseEntity.ok(buildReturnStatus(order, "Item picked up"));
    }

    /** PUT /api/orders/{orderId}/return/refund */
    @PutMapping("/{orderId}/return/refund")
    public ResponseEntity<OrderDto.ReturnStatusResponse> processRefund(
            @PathVariable String orderId) {

        Order order = orderService.processRefund(orderId);
        return ResponseEntity.ok(buildReturnStatus(order, "Refund processed"));
    }

    /** PUT /api/orders/{orderId}/return/complete */
    @PutMapping("/{orderId}/return/complete")
    public ResponseEntity<OrderDto.ReturnStatusResponse> completeReturn(
            @PathVariable String orderId) {

        Order order = orderService.completeReturn(orderId);
        return ResponseEntity.ok(buildReturnStatus(order, "Return completed successfully"));
    }

    // ── DELETE ─────────────────────────────────────────────────────────────

    /**
     * DELETE /api/orders/{orderId}
     * Hard delete — admin only. Use with caution.
     */
    @DeleteMapping("/{orderId}")
    public ResponseEntity<Map<String, String>> deleteOrder(
            @PathVariable String orderId) {

        orderService.deleteOrder(orderId);
        return ResponseEntity.ok(Map.of("message", "Order " + orderId + " deleted"));
    }

    // ── Mapping helpers ─────────────────────────────────────────────────────

    private OrderDto.Response mapToResponse(Order order) {
        return modelMapper.map(order, OrderDto.Response.class);
    }

    private OrderDto.ReturnStatusResponse buildReturnStatus(Order order, String message) {
        OrderDto.ReturnStatusResponse rs = new OrderDto.ReturnStatusResponse();
        rs.setOrderId(order.getId());
        rs.setStatus(order.getStatus());
        rs.setMessage(message);
        return rs;
    }
}