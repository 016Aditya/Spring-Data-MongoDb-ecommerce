package learnMongoDb.learnSpringMongoDb.repository;

import learnMongoDb.learnSpringMongoDb.entity.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProductRepository extends MongoRepository<Product, String> {

    List<Product> findByCategory(String category);

    List<Product> findBySubcategory(String subcategory); // ✅ Added

    List<Product> findByCategoryAndSubcategory(String category, String subcategory); // ✅ Added

    List<Product> findByBrandIgnoreCase(String brand); // ✅ Added

    List<Product> findByNameContainingIgnoreCase(String keyword);

    List<Product> findByPriceBetween(double min, double max); // ✅ Added

    List<Product> findByFeaturedTrue(); // ✅ Added

}