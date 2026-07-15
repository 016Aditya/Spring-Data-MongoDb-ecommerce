package learnMongoDb.learnSpringMongoDb.exception;

/**
 * InventoryConflictException — Commit 3
 *
 * Thrown ONLY when inventory changed between Phase A (validation) and
 * Phase B (atomic update) — i.e., another concurrent request won the race.
 *
 * Do NOT use for:
 *   - Invalid quantity          → use IllegalArgumentException
 *   - Out of stock at validation time → use InsufficientStockException
 *   - Purchase limit exceeded   → use MaxQuantityExceededException
 *
 * Maps to HTTP 409 Conflict via GlobalExceptionHandler.
 */
public class InventoryConflictException extends RuntimeException {

    private final String productId;

    public InventoryConflictException(String productId) {
        super("Inventory changed while placing your order for product: " + productId);
        this.productId = productId;
    }

    public InventoryConflictException(String productId, String message) {
        super(message);
        this.productId = productId;
    }

    public String getProductId() {
        return productId;
    }
}