package learnMongoDb.learnSpringMongoDb.service;

import learnMongoDb.learnSpringMongoDb.entity.Product;
import learnMongoDb.learnSpringMongoDb.error.InsufficientStockException;
import learnMongoDb.learnSpringMongoDb.error.MaxQuantityExceededException;
import learnMongoDb.learnSpringMongoDb.error.ResourceNotFoundException;
import learnMongoDb.learnSpringMongoDb.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * InventoryValidationService — Commit 3
 *
 * Single source of truth for ALL inventory validation logic.
 * Replaces the validation responsibilities that were split between
 * CartValidator (Commit 1) and inline checks in OrderService (Commit 2).
 *
 * Responsibilities (in order):
 *   1. Validate product exists
 *   2. Validate product is active / available (inStock flag sanity check)
 *   3. Validate requested quantity <= current stock
 *   4. Validate requested quantity <= maxOrderQuantity
 *
 * IMPORTANT: This validation happens in Phase A of checkout, BEFORE any
 * atomic stock mutation. Because MongoDB reads here are NOT locked,
 * stock may still change between this validation and the atomic update
 * in InventoryService — that race is handled by InventoryConflictException,
 * not by this service.
 *
 * CartValidator (Commit 1) remains for the ADD-TO-CART flow, where no
 * order is being placed yet. This service is specifically for CHECKOUT-TIME
 * validation, which needs the same rules but is invoked from OrderService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryValidationService {

    private final ProductRepository productRepository;

    /**
     * Validate a single product + quantity pair.
     *
     * @param productId          product to validate
     * @param requestedQuantity  quantity the customer wants to purchase
     * @return the validated Product entity
     * @throws ResourceNotFoundException     product does not exist
     * @throws InsufficientStockException    requestedQuantity > current stock
     * @throws MaxQuantityExceededException  requestedQuantity > maxOrderQuantity
     */
    public Product validate(String productId, int requestedQuantity) {
        // 1 — Product exists
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found: " + productId));

        // 2 — Product is active / available
        if (product.getStock() <= 0) {
            throw new InsufficientStockException(0, requestedQuantity);
        }

        // 3 — Stock check
        if (requestedQuantity > product.getStock()) {
            throw new InsufficientStockException(product.getStock(), requestedQuantity);
        }

        // 4 — Purchase limit check
        int limit = product.getMaxOrderQuantity() > 0 ? product.getMaxOrderQuantity() : 10;
        if (requestedQuantity > limit) {
            throw new MaxQuantityExceededException(limit, requestedQuantity);
        }

        log.debug("Validation passed — productId={} stock={} requested={} limit={}",
                productId, product.getStock(), requestedQuantity, limit);

        return product;
    }

    /**
     * Validate an entire cart in one pass (Phase A of checkout).
     * Fails fast on the first invalid item — no partial validation state.
     *
     * @param productQuantities map of productId → requestedQuantity
     */
    public void validateCart(Map<String, Integer> productQuantities) {
        log.info("Validating {} cart item(s) before checkout", productQuantities.size());
        for (Map.Entry<String, Integer> entry : productQuantities.entrySet()) {
            validate(entry.getKey(), entry.getValue());
        }
        log.info("Cart validation passed for all {} item(s)", productQuantities.size());
    }
}