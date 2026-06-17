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
 *
 * averageRating and totalRatings are computed dynamically
 * from the Review collection — never set manually.
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

    /**
     * Whether this product is featured on the homepage.
     * Primitive boolean — Lombok generates isFeatured() correctly.
     */
    @Builder.Default
    private boolean featured = false;

    /**
     * Dynamically calculated average from the Review collection.
     * Updated by ReviewService whenever a review is added/updated/deleted.
     * Never seeded manually.
     */
    @Builder.Default
    private Double averageRating = 0.0;

    /**
     * Total number of reviews for this product.
     * Kept in sync with the Review collection by ReviewService.
     * Never seeded manually.
     */
    @Builder.Default
    private Integer totalRatings = 0;
}