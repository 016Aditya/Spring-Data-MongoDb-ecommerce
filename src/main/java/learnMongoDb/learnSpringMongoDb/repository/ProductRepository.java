package learnMongoDb.learnSpringMongoDb.repository;

import learnMongoDb.learnSpringMongoDb.entity.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProductRepository extends MongoRepository<Product,String> {

    List<Product> findByCategory(String category);
    // Finds products where the name contains the keyword (ignoring uppercase/lowercase)
    List<Product> findByNameContainingIgnoreCase(String keyword);
}
