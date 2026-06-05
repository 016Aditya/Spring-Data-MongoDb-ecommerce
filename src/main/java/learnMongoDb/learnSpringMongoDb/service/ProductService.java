package learnMongoDb.learnSpringMongoDb.service;

import learnMongoDb.learnSpringMongoDb.entity.Product;
import learnMongoDb.learnSpringMongoDb.repository.ProductRepository; // Assuming you fixed the spelling typo here!
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    // Lombok's @RequiredArgsConstructor will automatically inject this for us
    private final ProductRepository productRepository;

    public Product createProduct(Product product) {
        // Business logic could go here (e.g., checking if product name already exists)
        return productRepository.save(product);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(String id) {
        return productRepository.findById(id);
    }

    public Product updateProduct(String id, Product updatedData) {
        // 1. Find the existing product
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + id));

        // 2. Update the fields
        existingProduct.setName(updatedData.getName());
        existingProduct.setCategory(updatedData.getCategory());
        existingProduct.setPrice(updatedData.getPrice());

        // 3. Save and return (MongoDB will automatically update the @LastModifiedDate!)
        return productRepository.save(existingProduct);
    }
}