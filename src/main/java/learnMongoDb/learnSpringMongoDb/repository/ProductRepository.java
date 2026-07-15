package learnMongoDb.learnSpringMongoDb.repository;

import learnMongoDb.learnSpringMongoDb.entity.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    // --- The missing methods from the current screenshot ---
    List<Product> findByCategory(String category);

    List<Product> findBySubcategory(String subcategory);

    List<Product> findByCategoryAndSubcategory(String category, String subcategory);

    // --- The methods we added in the previous step ---
    List<Product> findByBrandIgnoreCase(String brand);

    List<Product> findByPriceBetween(double min, double max);

    List<Product> findByFeaturedTrue();
}