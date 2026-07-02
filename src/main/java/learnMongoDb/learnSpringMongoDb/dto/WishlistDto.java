package learnMongoDb.learnSpringMongoDb.dto;

import java.util.List;

public class WishlistDto {

    private String id;
    private String userId;
    private List<WishlistItem> items;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public List<WishlistItem> getItems() { return items; }
    public void setItems(List<WishlistItem> items) { this.items = items; }

    public static class WishlistItem {
        private String productId;
        private String name;       // → maps to productName in frontend
        private Double price;      // → maps to unitPrice in frontend
        private String imageUrl;
        private String brand;      // NEW
        private String category;   // NEW

        public String getProductId()           { return productId; }
        public void   setProductId(String v)   { this.productId = v; }
        public String getName()                { return name; }
        public void   setName(String v)        { this.name = v; }
        public Double getPrice()               { return price; }
        public void   setPrice(Double v)       { this.price = v; }
        public String getImageUrl()            { return imageUrl; }
        public void   setImageUrl(String v)    { this.imageUrl = v; }
        public String getBrand()               { return brand; }
        public void   setBrand(String v)       { this.brand = v; }
        public String getCategory()            { return category; }
        public void   setCategory(String v)    { this.category = v; }
    }
}