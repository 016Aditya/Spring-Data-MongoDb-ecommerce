//package learnMongoDb.learnSpringMongoDb;
//
//import learnMongoDb.learnSpringMongoDb.entity.Address;
//import learnMongoDb.learnSpringMongoDb.entity.Order;
//import learnMongoDb.learnSpringMongoDb.entity.Product;
//import learnMongoDb.learnSpringMongoDb.repository.OrderRepository;
//import learnMongoDb.learnSpringMongoDb.repository.ProductRepository;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.util.List;
//
//@SpringBootTest
//public class RelationshipMongoTests {
//
//    @Autowired
//    private OrderRepository orderRepository;
//
//    @Autowired
//    private ProductRepository productRepository;
//
//    @Test
//    public void testOrderCreation() {
//
//        Product laptop = Product.builder()
//                .name("Gaming Laptop")
//                .category("Electronics")
//                .price(1299.99)
//                .build();
//        laptop = productRepository.save(laptop);
//
//        Product phone = Product.builder()
//                .name("Gaming Phone")
//                .category("Electronics")
//                .price(199.99)
//                .build();
//        phone = productRepository.save(phone);
//
//        Order order = Order.builder()
//                .status("READY")
//                .quantity(5)
//                .totalPrice(500.0)
//                .products(List.of(laptop,phone))
//                .address(Address.builder()
//                        .line1("Line 1 Address")
//                        .city("Delhi")
//                        .state("Delhi")
//                        .build())
//                .build();
//
//        order = orderRepository.insert(order);
//
//        System.out.println("Inserted Order with nested address: " + order.getStatus());
//    }
//
//    @Test
//    public void testOrderFetch() {
////        var orders = orderRepository.findByAddressCity ("Delhi");
//        var orders = orderRepository.findbyCity("Delhi");
//
//        orders.forEach(System.out::println);
//    }
//}