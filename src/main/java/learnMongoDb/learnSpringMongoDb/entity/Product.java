package learnMongoDb.learnSpringMongoDb.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Product document stored in the "products" MongoDB collection.
 *
 * Hierarchy example:
 *   category    = "Electronics"    subcategory = "Mobile"
 *   category    = "Electronics"    subcategory = "Laptop"
 *   category    = "Clothing"       subcategory = "Shirt"
 *   category    = "Clothing"       subcategory = "Shoes"
 *   category    = "Books"          subcategory = "Stationery"
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Document(collection = "products")
public class Product {

    @Id
    private String id;

    private String name;

    /** Top-level category, e.g. Electronics, Clothing, Books */
    @Indexed
    private String category;

    /** Sub-type within a category, e.g. Mobile, Laptop, Shirt, Shoes */
    @Indexed
    private String subcategory;

    /** Brand name, e.g. Samsung, Nike, Penguin */
    private String brand;

    @Indexed
    private double price;

    /** Available stock count */
    private int stock;

    private String imageUrl;

    private String description;
}
