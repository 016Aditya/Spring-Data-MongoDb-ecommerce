package learnMongoDb.learnSpringMongoDb.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Document(collection = "products")
public class Product {
    @Id
    private String id;
    private String name;
    @Indexed
    private String category;
    @Indexed
    private double price;
    private String imageUrl;
    private String description;
}
