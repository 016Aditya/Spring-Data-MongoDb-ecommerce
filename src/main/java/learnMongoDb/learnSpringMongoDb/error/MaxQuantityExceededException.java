package learnMongoDb.learnSpringMongoDb.error;

import lombok.Getter;

/**
 * Thrown when a user tries to order more than the permitted limit per order.
 */
@Getter
public class MaxQuantityExceededException extends RuntimeException {

    private final int maxAllowed;
    private final int requestedQuantity;

    public MaxQuantityExceededException(int maxAllowed, int requestedQuantity) {
        super("Maximum " + maxAllowed + " items allowed per order.");
        this.maxAllowed = maxAllowed;
        this.requestedQuantity = requestedQuantity;
    }
}