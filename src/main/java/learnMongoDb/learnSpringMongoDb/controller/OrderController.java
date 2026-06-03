package learnMongoDb.learnSpringMongoDb.controller;

import learnMongoDb.learnSpringMongoDb.entity.Order;
import learnMongoDb.learnSpringMongoDb.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        return ResponseEntity.ok(orderService.createOrder(order));
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<List<Order>> getOrdersByCity(@PathVariable String city) {
        return ResponseEntity.ok(orderService.getOrdersByCity(city));
    }

    @GetMapping("/status")
    public ResponseEntity<List<Order>> getOrdersByStatusAndPrice(
            @RequestParam String status,
            @RequestParam double minPrice) {
        return ResponseEntity.ok(orderService.getOrdersByStatusAndPrice(status, minPrice));
    }
}