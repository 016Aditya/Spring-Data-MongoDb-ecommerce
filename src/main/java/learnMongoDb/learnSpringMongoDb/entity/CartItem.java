package learnMongoDb.learnSpringMongoDb.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartItem {
    private String productId;
    private Integer quantity;
    private Double unitPrice;
}