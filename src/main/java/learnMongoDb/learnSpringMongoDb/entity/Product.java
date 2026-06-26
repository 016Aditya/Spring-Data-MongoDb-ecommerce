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
 * averageRating and totalRatings are managed exclusively by ReviewService.
 * They are recalculated after every review add / update / delete.
 * Never set them manually.
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

    @Indexed
    private String category;

    @Indexed
    private String subcategory;

    private String brand;

    @Indexed
    private double price;

    /**
     * Original / MRP price before any discount.
     * If set and greater than price, the frontend shows a strikethrough
     * original price and a discount percentage badge.
     */
    private double originalPrice;

    private int stock;

    private String imageUrl;

    private String description;

    @Builder.Default
    private boolean featured = false;

    /**
     * Dynamically calculated average from the Review collection.
     * Updated by ReviewService — never seed manually.
     */
    @Builder.Default
    private Double averageRating = 0.0;

    /**
     * Total number of reviews for this product.
     * Kept in sync by ReviewService — never seed manually.
     */
    @Builder.Default
    private Integer totalRatings = 0;
}