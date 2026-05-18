package learnMongoDb.learnSpringMongoDb;

import learnMongoDb.learnSpringMongoDb.entity.Order;
import learnMongoDb.learnSpringMongoDb.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SimpleMongoTests {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    public void testCreateOrder(){
        Order order = Order.builder()
                .status("Ready")
                .quantity(31)
                .totalPrice(350.0)
                .build();

        order = orderRepository.insert(order);

        System.out.println(order);
    }
}
