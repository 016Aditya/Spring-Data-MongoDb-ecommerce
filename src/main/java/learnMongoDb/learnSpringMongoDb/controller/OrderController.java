package learnMongoDb.learnSpringMongoDb.controller;

import learnMongoDb.learnSpringMongoDb.dto.OrderDto;
import learnMongoDb.learnSpringMongoDb.entity.Order;
import learnMongoDb.learnSpringMongoDb.security.CustomUserDetails;
import learnMongoDb.learnSpringMongoDb.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper; // Added import
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final ModelMapper modelMapper; // ── Inject ModelMapper ──

    // ... All your HTTP endpoints remain exactly the same ...
    // (createOrder, getOrdersByUser, initiateReturn, etc.)

    // ── Refactored Mapping Helper ────────────────────────────────────

    private OrderDto.Response mapToResponse(Order order) {
        // ModelMapper handles all standard fields, plus the legacy
        // fallbacks and custom image fields defined in the config.
        return modelMapper.map(order, OrderDto.Response.class);
    }
}