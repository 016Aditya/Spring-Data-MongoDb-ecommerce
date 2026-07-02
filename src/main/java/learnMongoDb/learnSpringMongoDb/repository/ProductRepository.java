package learnMongoDb.learnSpringMongoDb.repository;

import learnMongoDb.learnSpringMongoDb.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ProductRepository extends MongoRepository<Product, String> {

    List<Product> findByCategory(String category);

    List<Product> findBySubcategory(String subcategory);

    List<Product> findByCategoryAndSubcategory(String category, String subcategory);

    List<Product> findByBrandIgnoreCase(String brand);

    List<Product> findByPriceBetween(double min, double max);

    List<Product> findByFeaturedTrue();

    // ── Legacy Search (Can be deprecated/removed if no longer used) ────────
    List<Product> findByNameContainingIgnoreCase(String keyword);

    // ── NEW: Paginated Full Search ──────────────────────────────────────────
    Page<Product> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    // ── NEW: Lightweight Suggestions Projection ─────────────────────────────
    // Uses fields = "{...}" so MongoDB only returns the fields we absolutely need over the wire
    @Query(value = "{ 'name': { $regex: ?0, $options: 'i' } }", fields = "{ 'id': 1, 'name': 1, 'category': 1, 'imageUrl': 1 }")
    List<Product> findSuggestionsByNameRegex(String safeRegex, Pageable pageable);
}