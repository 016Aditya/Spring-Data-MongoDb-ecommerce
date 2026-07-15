package learnMongoDb.learnSpringMongoDb.error;

import lombok.Getter;

/**
 * Thrown when a user tries to add to cart or checkout more items than are available.
 */
@Getter
public class InsufficientStockException extends RuntimeException {

    private final int availableStock;
    private final int requestedQuantity;

    public InsufficientStockException(int availableStock, int requestedQuantity) {
        super("Only " + availableStock + " items left.");
        this.availableStock = availableStock;
        this.requestedQuantity = requestedQuantity;
    }
}