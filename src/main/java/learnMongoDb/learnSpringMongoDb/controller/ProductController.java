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

    // ─── CREATE ────────────────────────────────────────────────────────────────

    /**
     * POST /api/products
     * Body example:
     * {
     *   "name":        "Samsung Galaxy S24",
     *   "category":    "Electronics",
     *   "subcategory": "Mobile",
     *   "brand":       "Samsung",
     *   "price":       79999,
     *   "stock":       50,
     *   "imageUrl":    "https://...",
     *   "description": "Latest Samsung flagship",
     *   "featured":    true
     * }
     */
    @PostMapping
    public ResponseEntity<ProductDto.Response> createProduct(@RequestBody ProductDto.Request request) {
        Product saved = productService.createProduct(toEntity(request));
        return ResponseEntity.ok(toResponse(saved));
    }

    // ─── READ – all / by ID ───────────────────────────────────────────────────

    /** GET /api/products */
    @GetMapping
    public ResponseEntity<List<ProductDto.Response>> getAllProducts() {
        return ResponseEntity.ok(mapList(productService.getAllProducts()));
    }

    /** GET /api/products/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto.Response> getProductById(@PathVariable String id) {
        return productService.getProductById(id)
                .map(p -> ResponseEntity.ok(toResponse(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ─── READ – featured ──────────────────────────────────────────────────────

    /**
     * GET /api/products/featured
     * Returns all products where featured = true.
     * Used by the React homepage FeaturedProducts section.
     * Returns 200 with an empty array [] if no products are featured yet
     * (the frontend hook falls back to latest-6 in that case).
     */
    @GetMapping("/featured")
    public ResponseEntity<List<ProductDto.Response>> getFeaturedProducts() {
        return ResponseEntity.ok(mapList(productService.getFeaturedProducts()));
    }

    // ─── READ – category / subcategory filters ────────────────────────────────

    /**
     * GET /api/products/category/{category}
     * e.g. /api/products/category/Electronics  → all electronics
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductDto.Response>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(mapList(productService.getProductsByCategory(category)));
    }

    /**
     * GET /api/products/category/{category}/subcategory/{subcategory}
     * e.g. /api/products/category/Electronics/subcategory/Mobile  → all mobiles
     *      /api/products/category/Clothing/subcategory/Shoes       → all shoes
     */
    @GetMapping("/category/{category}/subcategory/{subcategory}")
    public ResponseEntity<List<ProductDto.Response>> getByCategoryAndSubcategory(
            @PathVariable String category,
            @PathVariable String subcategory) {
        return ResponseEntity.ok(
                mapList(productService.getProductsByCategoryAndSubcategory(category, subcategory)));
    }

    /**
     * GET /api/products/subcategory/{subcategory}
     * e.g. /api/products/subcategory/Laptop
     */
    @GetMapping("/subcategory/{subcategory}")
    public ResponseEntity<List<ProductDto.Response>> getBySubcategory(@PathVariable String subcategory) {
        return ResponseEntity.ok(mapList(productService.getProductsBySubcategory(subcategory)));
    }

    // ─── READ – brand / search / price ───────────────────────────────────────

    /**
     * GET /api/products/brand/{brand}
     * e.g. /api/products/brand/Nike
     */
    @GetMapping("/brand/{brand}")
    public ResponseEntity<List<ProductDto.Response>> getByBrand(@PathVariable String brand) {
        return ResponseEntity.ok(mapList(productService.getProductsByBrand(brand)));
    }

    /**
     * GET /api/products/search?keyword=galaxy
     */
    @GetMapping("/search")
    public ResponseEntity<List<ProductDto.Response>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(mapList(productService.searchProductsByName(keyword)));
    }

    /**
     * GET /api/products/price?min=500&max=5000
     */
    @GetMapping("/price")
    public ResponseEntity<List<ProductDto.Response>> getByPriceRange(
            @RequestParam double min,
            @RequestParam double max) {
        return ResponseEntity.ok(mapList(productService.getProductsByPriceRange(min, max)));
    }

    // ─── UPDATE ────────────────────────────────────────────────────────────────

    /** PUT /api/products/{id} */
    @PutMapping("/{id}")
    public ResponseEntity<ProductDto.Response> updateProduct(
            @PathVariable String id,
            @RequestBody ProductDto.UpdateRequest request) {

        Product updateData = Product.builder()
                .name(request.getName())
                .category(request.getCategory())
                .subcategory(request.getSubcategory())
                .brand(request.getBrand())
                .price(request.getPrice())
                .stock(request.getStock())
                .imageUrl(request.getImageUrl())
                .description(request.getDescription())
                .featured(request.isFeatured())   // ✅ was missing before
                .build();

        return ResponseEntity.ok(toResponse(productService.updateProduct(id, updateData)));
    }

    /**
     * PATCH /api/products/{id}/featured?featured=true
     * Lightweight admin endpoint to toggle featured without a full PUT.
     * Example: PATCH /api/products/abc123/featured?featured=true
     */
    @PatchMapping("/{id}/featured")
    public ResponseEntity<ProductDto.Response> toggleFeatured(
            @PathVariable String id,
            @RequestParam boolean featured) {
        return ResponseEntity.ok(toResponse(productService.toggleFeatured(id, featured)));
    }

    // ─── DELETE ────────────────────────────────────────────────────────────────

    /** DELETE /api/products/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private Product toEntity(ProductDto.Request r) {
        return Product.builder()
                .name(r.getName())
                .category(r.getCategory())
                .subcategory(r.getSubcategory())
                .brand(r.getBrand())
                .price(r.getPrice())
                .stock(r.getStock())
                .imageUrl(r.getImageUrl())
                .description(r.getDescription())
                .featured(r.isFeatured())
                .build();
    }

    private ProductDto.Response toResponse(Product p) {
        ProductDto.Response res = new ProductDto.Response();
        res.setId(p.getId());
        res.setName(p.getName());
        res.setCategory(p.getCategory());
        res.setSubcategory(p.getSubcategory());
        res.setBrand(p.getBrand());
        res.setPrice(p.getPrice());
        res.setStock(p.getStock());
        res.setImageUrl(p.getImageUrl());
        res.setDescription(p.getDescription());
        res.setFeatured(p.isFeatured());                                  // ✅ was missing
        res.setAverageRating(p.getAverageRating() != null ? p.getAverageRating() : 0.0);   // ✅ was missing
        res.setTotalRatings(p.getTotalRatings() != null ? p.getTotalRatings() : 0);        // ✅ was missing
        return res;
    }

    private List<ProductDto.Response> mapList(List<Product> products) {
        return products.stream().map(this::toResponse).collect(Collectors.toList());
    }
}
