package learnMongoDb.learnSpringMongoDb.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@Document(collection = "carts")
public class ShoppingCart {

    @Id
    private String id;

    @Indexed(unique = true)
    private String userId; // Each user only gets one active cart

    private List<CartItem> items; // Embedded list of items

    private Double cartTotal;
}