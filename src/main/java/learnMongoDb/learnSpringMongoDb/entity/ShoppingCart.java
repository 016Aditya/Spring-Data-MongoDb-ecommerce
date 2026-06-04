package learnMongoDb.learnSpringMongoDb.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
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