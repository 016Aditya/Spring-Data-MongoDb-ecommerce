package learnMongoDb.learnSpringMongoDb.repository;

import learnMongoDb.learnSpringMongoDb.entity.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface OrderRepository extends MongoRepository<Order, String> {

    @Query("{ 'status': ?0, 'totalPrice': { $gte: ?1 } }")
    List<Order> findOrdersByStatusAndPrice(String status, double minPrice);

    List<Order> findByAddressCity(String city);

    @Query(value = "{'address.city': ?0 }", fields = "{'_id':1, 'quantity':1}")
    List<Order> findbyCity(String city);

    // New method added to support the User entity
    List<Order> findByUserId(String userId);
}