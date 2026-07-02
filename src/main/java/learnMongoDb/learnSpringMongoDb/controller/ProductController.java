package learnMongoDb.learnSpringMongoDb.controller;

import learnMongoDb.learnSpringMongoDb.dto.PagedResponse;
import learnMongoDb.learnSpringMongoDb.dto.ProductDto;
import learnMongoDb.learnSpringMongoDb.dto.ProductSuggestionDto;
import learnMongoDb.learnSpringMongoDb.entity.Product;
import learnMongoDb.learnSpringMongoDb.service.ProductQueryService;
import learnMongoDb.learnSpringMongoDb.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductQueryService productQueryService; // ── NEW ──
    private final ModelMapper modelMapper;

    // ─── CREATE ────────────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<ProductDto.Response> createProduct(@RequestBody ProductDto.Request request) {
        Product productToSave = modelMapper.map(request, Product.class);
        Product saved = productService.createProduct(productToSave);
        return ResponseEntity.ok(modelMapper.map(saved, ProductDto.Response.class));
    }

    // ─── READ – all / by ID ───────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<ProductDto.Response>> getAllProducts() {
        return ResponseEntity.ok(mapList(productService.getAllProducts()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto.Response> getProductById(@PathVariable String id) {
        return productService.getProductById(id)
                .map(p -> ResponseEntity.ok(modelMapper.map(p, ProductDto.Response.class)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ─── READ – featured ──────────────────────────────────────────────────────

    @GetMapping("/featured")
    public ResponseEntity<List<ProductDto.Response>> getFeaturedProducts() {
        return ResponseEntity.ok(mapList(productService.getFeaturedProducts()));
    }

    // ─── READ – category / subcategory filters ────────────────────────────────

    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductDto.Response>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(mapList(productService.getProductsByCategory(category)));
    }

    @GetMapping("/category/{category}/subcategory/{subcategory}")
    public ResponseEntity<List<ProductDto.Response>> getByCategoryAndSubcategory(
            @PathVariable String category,
            @PathVariable String subcategory) {
        return ResponseEntity.ok(
                mapList(productService.getProductsByCategoryAndSubcategory(category, subcategory)));
    }

    @GetMapping("/subcategory/{subcategory}")
    public ResponseEntity<List<ProductDto.Response>> getBySubcategory(@PathVariable String subcategory) {
        return ResponseEntity.ok(mapList(productService.getProductsBySubcategory(subcategory)));
    }

    // ─── READ – brand / search / price ───────────────────────────────────────

    @GetMapping("/brand/{brand}")
    public ResponseEntity<List<ProductDto.Response>> getByBrand(@PathVariable String brand) {
        return ResponseEntity.ok(mapList(productService.getProductsByBrand(brand)));
    }

    // ─── NEW: Paginated Search ────────────────────────────────────────────────
    @GetMapping("/search")
    public ResponseEntity<PagedResponse<ProductDto.Response>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        // Prevent unreasonably massive queries crashing the DB
        if (keyword.trim().length() > 100) return ResponseEntity.badRequest().build();

        Page<Product> productPage = productQueryService.search(keyword, page, size);

        // Map raw Entities into the standard ProductDto.Response to ensure dynamically computed fields are populated
        List<ProductDto.Response> dtoList = productPage.getContent().stream()
                .map(p -> modelMapper.map(p, ProductDto.Response.class))
                .collect(Collectors.toList());

        return ResponseEntity.ok(PagedResponse.of(dtoList, productPage));
    }

    // ─── NEW: Lightweight Suggestions ─────────────────────────────────────────
    @GetMapping("/suggestions")
    public ResponseEntity<List<ProductSuggestionDto>> suggestions(@RequestParam String q) {
        return ResponseEntity.ok(productQueryService.suggestions(q));
    }

    @GetMapping("/price")
    public ResponseEntity<List<ProductDto.Response>> getByPriceRange(
            @RequestParam double min,
            @RequestParam double max) {
        return ResponseEntity.ok(mapList(productService.getProductsByPriceRange(min, max)));
    }

    // ─── UPDATE ────────────────────────────────────────────────────────────────

    @PutMapping("/{id}")
    public ResponseEntity<ProductDto.Response> updateProduct(
            @PathVariable String id,
            @RequestBody ProductDto.UpdateRequest request) {

        Product updateData = modelMapper.map(request, Product.class);
        Product updated = productService.updateProduct(id, updateData);
        return ResponseEntity.ok(modelMapper.map(updated, ProductDto.Response.class));
    }

    @PatchMapping("/{id}/featured")
    public ResponseEntity<ProductDto.Response> toggleFeatured(
            @PathVariable String id,
            @RequestParam boolean featured) {
        Product updated = productService.toggleFeatured(id, featured);
        return ResponseEntity.ok(modelMapper.map(updated, ProductDto.Response.class));
    }

    // ─── DELETE ────────────────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private List<ProductDto.Response> mapList(List<Product> products) {
        return products.stream()
                .map(p -> modelMapper.map(p, ProductDto.Response.class))
                .collect(Collectors.toList());
    }
}