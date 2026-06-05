package learnMongoDb.learnSpringMongoDb.service;

import learnMongoDb.learnSpringMongoDb.entity.Order;
import learnMongoDb.learnSpringMongoDb.entity.Product;
import learnMongoDb.learnSpringMongoDb.repository.OrderRepository;
import learnMongoDb.learnSpringMongoDb.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public Order createOrder(Order order) {
        double calculatedTotal = 0.0;

        // 1. Calculate the real total price based on the DB products, not the client's payload
        if (order.getProducts() != null && !order.getProducts().isEmpty()) {
            for (Product requestProduct : order.getProducts()) {
                Optional<Product> dbProduct = productRepository.findById(requestProduct.getId());

                if (dbProduct.isPresent()) {
                    // Assuming the 'quantity' field on Order applies to the whole order
                    calculatedTotal += (dbProduct.get().getPrice() * order.getQuantity());
                } else {
                    throw new RuntimeException("Product with ID " + requestProduct.getId() + " not found!");
                }
            }
        }

        // 2. Set the calculated total
        order.setTotalPrice(calculatedTotal);

        // 3. Set a default status if the client didn't provide one
        if (order.getStatus() == null || order.getStatus().isEmpty()) {
            order.setStatus("PENDING");
        }

        // 4. Save to MongoDB
        return orderRepository.save(order);
    }

    // Leveraging the custom queries you wrote earlier
    public List<Order> getOrdersByStatusAndPrice(String status, double minPrice) {
        return orderRepository.findOrdersByStatusAndPrice(status, minPrice);
    }

    public List<Order> getOrdersByCity(String city) {
        return orderRepository.findByAddressCity(city);
    }

    public Order updateOrderStatus(String orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        order.setStatus(newStatus);

        return orderRepository.save(order);
    }
}