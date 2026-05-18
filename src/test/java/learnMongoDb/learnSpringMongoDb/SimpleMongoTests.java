package learnMongoDb.learnSpringMongoDb;

import learnMongoDb.learnSpringMongoDb.entity.Order;
import learnMongoDb.learnSpringMongoDb.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import org.springframework.data.domain.Pageable;
import java.util.List;

@SpringBootTest
public class SimpleMongoTests {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    public void testCreateOrder() {
        for (int i=2;i<12;i++) {
            Order order = Order.builder()
                    .status("Ready")
                    .quantity(2*i)
                    .totalPrice(100.0*i)
                    .build();

            order = orderRepository.insert(order);
        }
    }

    @Test
    public void testGetOrder() {
        // 0 = First Page, 5 = Fetch up to 5 documents
        Pageable pageRequest = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "totalPrice"));

        // findAll(Pageable) returns a Page object; .toList() converts its content cleanly
        List<Order> orderList = orderRepository.findAll(pageRequest).toList();

        // Verify if we actually got items back
        if (orderList.isEmpty()) {
            System.out.println("No orders found on this page! Check your database counts.");
        } else {
            orderList.forEach(System.out::println);
        }
    }

    @Test
    public void deleteOrder() {
        List<Order> orderList = orderRepository.findOrdersByStatusAndPrice("Ready", 300);        orderList.forEach(System.out::println);
        orderRepository.deleteAll(orderList);
        orderList = orderRepository.findOrdersByStatusAndPrice ("READY", 30);
        orderList.forEach(System.out::println); // should be empty
    }
}
