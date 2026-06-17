package learnMongoDb.learnSpringMongoDb.dto;

import lombok.Data;

public class ProductDto {

    /** Used when CREATING a product (POST /api/products) */
    @Data
    public static class Request {
        private String name;
        private String category;      // e.g. Electronics
        private String subcategory;   // e.g. Mobile, Laptop
        private String brand;
        private double price;
        private int    stock;
        private String imageUrl;
        private String description;
        private boolean featured;
    }

    /** Returned in ALL product responses */
    @Data
    public static class Response {
        private String  id;
        private String  name;
        private String  category;
        private String  subcategory;
        private String  brand;
        private double  price;
        private int     stock;
        private String  imageUrl;
        private String  description;
        private boolean featured;
        private Double  averageRating;
        private Integer totalRatings;
    }

    /** Used when UPDATING a product (PUT /api/products/{id}) */
    @Data
    public static class UpdateRequest {
        private String name;
        private String category;
        private String subcategory;
        private String brand;
        private double price;
        private int    stock;
        private String imageUrl;
        private String description;
        private boolean featured;
    }
}