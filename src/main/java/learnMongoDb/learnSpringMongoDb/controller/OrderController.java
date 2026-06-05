package learnMongoDb.learnSpringMongoDb.controller;

import learnMongoDb.learnSpringMongoDb.dto.OrderDto;
import learnMongoDb.learnSpringMongoDb.entity.Order;
import learnMongoDb.learnSpringMongoDb.entity.Product;
import learnMongoDb.learnSpringMongoDb.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderDto.Response> createOrder(@RequestBody OrderDto.Request request) {

        // 1. Map the string IDs from the DTO into a list of Product entities (with just the ID set)
        List<Product> requestedProducts = request.getProductIds().stream()
                .map(id -> Product.builder().id(id).build())
                .collect(Collectors.toList());

        // 2. Build the raw Order entity for the Service layer
        Order orderToProcess = Order.builder()
                .userId(request.getUserId())
                .quantity(request.getQuantity())
                .address(request.getAddress())
                .products(requestedProducts)
                .build();

        // 3. Let the Service validate prices and save to MongoDB
        Order savedOrder = orderService.createOrder(orderToProcess);

        // 4. Map the database result back to a clean Response DTO
        return ResponseEntity.ok(mapToResponse(savedOrder));
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<List<OrderDto.Response>> getOrdersByCity(@PathVariable String city) {
        List<OrderDto.Response> responses = orderService.getOrdersByCity(city).stream()
                .map(this::mapToResponse) // Maps every order in the list to a DTO
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/status")
    public ResponseEntity<List<OrderDto.Response>> getOrdersByStatusAndPrice(
            @RequestParam String status,
            @RequestParam double minPrice) {

        List<OrderDto.Response> responses = orderService.getOrdersByStatusAndPrice(status, minPrice).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderDto.Response> updateOrderStatus(
            @PathVariable String id,
            @RequestBody OrderDto.UpdateStatusRequest request) {

        Order updatedOrder = orderService.updateOrderStatus(id, request.getStatus());

        return ResponseEntity.ok(mapToResponse(updatedOrder));
    }

    // --- Helper Method ---

    // Translates a database Order into a safe JSON Response
    private OrderDto.Response mapToResponse(Order order) {
        OrderDto.Response response = new OrderDto.Response();
        response.setId(order.getId());
        response.setUserId(order.getUserId());
        response.setQuantity(order.getQuantity());
        response.setTotalPrice(order.getTotalPrice());
        response.setStatus(order.getStatus());
        response.setAddress(order.getAddress());
        response.setCreatedAt(order.getCreatedAt());
        return response;
    }
}