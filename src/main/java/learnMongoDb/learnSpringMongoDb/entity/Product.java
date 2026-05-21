package learnMongoDb.learnSpringMongoDb.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation. Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

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
}