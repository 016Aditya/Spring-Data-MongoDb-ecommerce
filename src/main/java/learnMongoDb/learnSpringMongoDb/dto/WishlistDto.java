package learnMongoDb.learnSpringMongoDb.dto;

import java.util.List;

public class WishlistDto {

    private String id;
    private String userId;
    private List<WishlistItem> items;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public List<WishlistItem> getItems() { return items; }
    public void setItems(List<WishlistItem> items) { this.items = items; }

    // Nested class for the items
    public static class WishlistItem {
        private String productId;
        private String name;
        private Double price;
        private String imageUrl; // Matches your frontend requirement

        // Getters and Setters
        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    }
}