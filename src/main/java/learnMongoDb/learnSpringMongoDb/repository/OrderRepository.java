package learnMongoDb.learnSpringMongoDb.repository;

import learnMongoDb.learnSpringMongoDb.entity.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface OrderRepository extends MongoRepository<Order,String> {

    @Query("{ 'status': ?0, 'totalPrice': { $gte: ?1 } }")
    List<Order> findOrdersByStatusAndPrice(String status, double minPrice);

//    List<Order> findByAddressCity(String  city);

}
