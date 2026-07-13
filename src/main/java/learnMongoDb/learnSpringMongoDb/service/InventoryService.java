package learnMongoDb.learnSpringMongoDb.service;

import learnMongoDb.learnSpringMongoDb.entity.Product;
import learnMongoDb.learnSpringMongoDb.error.InsufficientStockException;
import learnMongoDb.learnSpringMongoDb.error.ResourceNotFoundException;
import learnMongoDb.learnSpringMongoDb.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * InventoryService — Commit 2
 * Sole owner of all inventory mutation logic.
 * OrderService MUST delegate all stock changes here.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ProductRepository productRepository;

    // ── Decrease stock (single item) ──────────────────────────────────
    public Product decreaseStock(String productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found during stock deduction: " + productId));

        int currentStock = product.getStock();
        int newStock     = currentStock - quantity;

        if (newStock < 0) {
            throw new InsufficientStockException(currentStock, quantity);
        }

        product.setStock(newStock);
        product.setInStock(newStock > 0);

        Product saved = productRepository.save(product);
        log.info("Stock decreased — productId={} prev={} deducted={} new={} inStock={}",
                productId, currentStock, quantity, newStock, saved.isInStock());
        return saved;
    }

    // ── Decrease stock (batch) — returns snapshot for compensation ────
    public Map<String, Integer> decreaseStockBatch(Map<String, Integer> quantities) {
        Map<String, Integer> previousStocks = new HashMap<>();

        for (Map.Entry<String, Integer> entry : quantities.entrySet()) {
            String productId = entry.getKey();
            int    qty       = entry.getValue();

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found: " + productId));

            previousStocks.put(productId, product.getStock());

            int newStock = product.getStock() - qty;
            if (newStock < 0) {
                throw new InsufficientStockException(product.getStock(), qty);
            }

            product.setStock(newStock);
            product.setInStock(newStock > 0);
            productRepository.save(product);

            log.info("Batch stock decrease — productId={} -{} → stock={}",
                    productId, qty, newStock);
        }

        return previousStocks;
    }

    // ── Compensation: restore stock on order save failure ─────────────
    public void restoreStock(Map<String, Integer> previousStocks) {
        for (Map.Entry<String, Integer> entry : previousStocks.entrySet()) {
            String productId     = entry.getKey();
            int    restoreAmount = entry.getValue();

            productRepository.findById(productId).ifPresent(product -> {
                product.setStock(restoreAmount);
                product.setInStock(restoreAmount > 0);
                productRepository.save(product);
                log.warn("Stock restored (compensation) — productId={} restoredTo={}",
                        productId, restoreAmount);
            });
        }
    }

    // ── Future hooks (Commit 3+) ───────────────────────────────────────
    public Product increaseStock(String productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found: " + productId));
        int newStock = product.getStock() + quantity;
        product.setStock(newStock);
        product.setInStock(true);
        return productRepository.save(product);
    }
}