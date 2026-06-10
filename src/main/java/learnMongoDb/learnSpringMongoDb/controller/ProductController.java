package learnMongoDb.learnSpringMongoDb.controller;

import learnMongoDb.learnSpringMongoDb.dto.ProductDto;
import learnMongoDb.learnSpringMongoDb.entity.Product;
import learnMongoDb.learnSpringMongoDb.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductDto.Response> createProduct(@RequestBody ProductDto.Request request) {
        Product productToSave = Product.builder()
                .name(request.getName())
                .category(request.getCategory())
                .price(request.getPrice())
                .imageUrl(request.getImageUrl())
                .description(request.getDescription())
                .build();

        Product savedProduct = productService.createProduct(productToSave);
        return ResponseEntity.ok(mapToResponse(savedProduct));
    }

    @GetMapping
    public ResponseEntity<List<ProductDto.Response>> getAllProducts() {
        List<ProductDto.Response> responses = productService.getAllProducts().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto.Response> getProductById(@PathVariable String id) {
        return productService.getProductById(id)
                .map(product -> ResponseEntity.ok(mapToResponse(product)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDto.Response> updateProduct(
            @PathVariable String id,
            @RequestBody ProductDto.UpdateRequest request) {

        Product productUpdateData = Product.builder()
                .name(request.getName())
                .category(request.getCategory())
                .price(request.getPrice())
                .imageUrl(request.getImageUrl())
                .description(request.getDescription())
                .build();

        Product updatedProduct = productService.updateProduct(id, productUpdateData);
        return ResponseEntity.ok(mapToResponse(updatedProduct));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductDto.Response>> getProductsByCategory(@PathVariable String category) {
        List<ProductDto.Response> responses = productService.getProductsByCategory(category).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductDto.Response>> searchProducts(@RequestParam String keyword) {
        List<ProductDto.Response> responses = productService.searchProductsByName(keyword).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    private ProductDto.Response mapToResponse(Product product) {
        ProductDto.Response response = new ProductDto.Response();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setCategory(product.getCategory());
        response.setPrice(product.getPrice());
        response.setImageUrl(product.getImageUrl());
        response.setDescription(product.getDescription());
        return response;
    }
}
