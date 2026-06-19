package learnMongoDb.learnSpringMongoDb.controller;

import learnMongoDb.learnSpringMongoDb.dto.OrderDto;
import learnMongoDb.learnSpringMongoDb.entity.Order;
import learnMongoDb.learnSpringMongoDb.entity.OrderItem;
import learnMongoDb.learnSpringMongoDb.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * OrderController — REST API for orders.
 *
 * All write paths delegate snapshot-building to OrderService.
 * mapToResponse() always populates the items list so the frontend
 * never receives an order without product details.
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

    @PostMapping
    public ResponseEntity<OrderDto.Response> createOrder(
            @RequestBody OrderDto.Request request) {

        Order saved = orderService.createOrder(
                request.getProductIds(),
                request.getUserId(),
                request.getAddress());

        return ResponseEntity.ok(mapToResponse(saved));
    }

    // ── GET /api/orders/user/{userId} ────────────────────────────────────────

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderDto.Response>> getOrdersByUser(
            @PathVariable String userId) {

        List<OrderDto.Response> responses = orderService.getOrdersByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    // ── GET /api/orders/{id} ─────────────────────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto.Response> getOrderById(
            @PathVariable String id) {

        return ResponseEntity.ok(mapToResponse(orderService.getOrderById(id)));
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
    public ResponseEntity<OrderDto.Response> initiateReturn(
            @PathVariable String id,
            @RequestBody(required = false) OrderDto.ReturnRequest request) {

        String reason = (request != null && request.getReason() != null)
                ? request.getReason()
                : "";

        Order updated = orderService.returnOrder(id, reason);
        return ResponseEntity.ok(mapToResponse(updated));
    }

    // ── GET /api/orders/{id}/return — fetch return status ────────────────────

    @GetMapping("/{id}/return")
    public ResponseEntity<OrderDto.ReturnStatusResponse> getReturnStatus(
            @PathVariable String id) {

        Order order = orderService.getOrderById(id);
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
    public ResponseEntity<OrderDto.Response> cancelReturn(
            @PathVariable String id) {

        Order order = orderService.getOrderById(id);
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

    /**
     * Translates an Order entity into a safe JSON Response DTO.
     *
     * Item resolution priority:
     *   1. order.getItems()          — new orders (stored as "items" in MongoDB)
     *   2. order.getLegacyProducts() — old orders (stored as "products" in MongoDB)
     *   3. empty list                — truly empty orders (graceful fallback)
     *
     * imageUrl and productImage are both set to the same absolute URL so the
     * React frontend's normalizeOrderItem() resolves the image on its first
     * priority check (item.imageUrl) without needing to fall through.
     */
    private OrderDto.Response mapToResponse(Order order) {
        OrderDto.Response response = new OrderDto.Response();
        response.setId(order.getId());
        response.setUserId(order.getUserId());
        response.setQuantity(order.getQuantity());
        response.setTotalPrice(order.getTotalPrice());
        response.setStatus(order.getStatus());
        response.setAddress(order.getAddress());
        response.setCreatedAt(order.getCreatedAt());

        // Prefer new `items` field; fall back to legacy `products` field for old documents.
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

                    // Set BOTH fields to the same URL:
                    //   imageUrl      — first priority in normalizeOrderItem()
                    //   productImage  — fallback / backward compat
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
