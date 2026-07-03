package learnMongoDb.learnSpringMongoDb.repository;

import learnMongoDb.learnSpringMongoDb.entity.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    List<Product> findByCategory(String category);

    List<Product> findBySubcategory(String subcategory);

    List<Product> findByCategoryAndSubcategory(String category, String subcategory);

    List<Product> findByBrandIgnoreCase(String brand);

    List<Product> findByPriceBetween(double min, double max);

    List<Product> findByFeaturedTrue();

    // Note: Complex multi-field search and suggestions have been moved
    // to ProductQueryService using MongoTemplate for better performance and security.
}