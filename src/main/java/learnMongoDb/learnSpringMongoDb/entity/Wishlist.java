package learnMongoDb.learnSpringMongoDb.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "wishlists")
public class Wishlist {

    @Id
    private String id;
    private String userId;
    private List<String> productIds = new ArrayList<>();

    // Constructors
    public Wishlist() {}

    public Wishlist(String userId) {
        this.userId = userId;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public List<String> getProductIds() { return productIds; }
    public void setProductIds(List<String> productIds) { this.productIds = productIds; }
}