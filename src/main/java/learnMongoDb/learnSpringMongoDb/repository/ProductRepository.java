package learnMongoDb.learnSpringMongoDb.repository;

import learnMongoDb.learnSpringMongoDb.entity.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProductRepository extends MongoRepository<Product, String> {

    /** All products in a top-level category, e.g. Electronics */
    List<Product> findByCategory(String category);

    /** All products in a specific subcategory, e.g. Mobile */
    List<Product> findBySubcategory(String subcategory);

    /** Drill-down: category + subcategory, e.g. Electronics > Mobile */
    List<Product> findByCategoryAndSubcategory(String category, String subcategory);

    /** Filter by brand */
    List<Product> findByBrandIgnoreCase(String brand);

    /** Full-text search across product names */
    List<Product> findByNameContainingIgnoreCase(String keyword);

    /** Products with price in a range */
    List<Product> findByPriceBetween(double minPrice, double maxPrice);
}
