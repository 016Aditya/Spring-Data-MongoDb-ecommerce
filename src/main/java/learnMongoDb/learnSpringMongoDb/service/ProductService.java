package learnMongoDb.learnSpringMongoDb.service;

import learnMongoDb.learnSpringMongoDb.entity.Product;
import learnMongoDb.learnSpringMongoDb.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    // ─── Create ───────────────────────────────────────────────────────────────

    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    // ─── Read ─────────────────────────────────────────────────────────────────

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(String id) {
        return productRepository.findById(id);
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    public List<Product> getProductsBySubcategory(String subcategory) {
        return productRepository.findBySubcategory(subcategory);
    }

    public List<Product> getProductsByCategoryAndSubcategory(String category, String subcategory) {
        return productRepository.findByCategoryAndSubcategory(category, subcategory);
    }

    public List<Product> getProductsByBrand(String brand) {
        return productRepository.findByBrandIgnoreCase(brand);
    }

    public List<Product> searchProductsByName(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword);
    }

    public List<Product> getProductsByPriceRange(double min, double max) {
        return productRepository.findByPriceBetween(min, max);
    }

    // ─── Update ───────────────────────────────────────────────────────────────

    public Product updateProduct(String id, Product updatedData) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + id));

        existing.setName(updatedData.getName());
        existing.setCategory(updatedData.getCategory());
        existing.setSubcategory(updatedData.getSubcategory());
        existing.setBrand(updatedData.getBrand());
        existing.setPrice(updatedData.getPrice());
        existing.setOriginalPrice(updatedData.getOriginalPrice());
        existing.setStock(updatedData.getStock());
        existing.setImageUrl(updatedData.getImageUrl());
        existing.setDescription(updatedData.getDescription());
        existing.setFeatured(updatedData.isFeatured());
        // NOTE: averageRating and totalRatings are managed by ReviewService — never overwrite them here

        return productRepository.save(existing);
    }

    // ─── Delete ───────────────────────────────────────────────────────────────

    public void deleteProduct(String id) {
        productRepository.deleteById(id);
    }

    // ─── Featured ─────────────────────────────────────────────────────────────

    public List<Product> getFeaturedProducts() {
        return productRepository.findByFeaturedTrue();
    }

    public Product toggleFeatured(String id, boolean featured) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
        product.setFeatured(featured);
        return productRepository.save(product);
    }
}