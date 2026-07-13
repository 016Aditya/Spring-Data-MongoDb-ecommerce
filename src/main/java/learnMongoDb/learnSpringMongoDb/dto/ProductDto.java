package learnMongoDb.learnSpringMongoDb.dto;

import lombok.Data;

public class ProductDto {

    /** Used when CREATING a product (POST /api/products) */
    @Data
    public static class Request {
        private String  name;
        private String  category;     // e.g. Electronics
        private String  subcategory;  // e.g. Mobile, Laptop
        private String  brand;
        private double  price;
        private double  originalPrice;
        private int     stock;
        private String  imageUrl;
        private String  description;
        private boolean featured;
    }

    /**
     * Returned in ALL product API responses.
     *
     * Field-name alignment with the React frontend:
     *   averageRating  → product.averageRating   (StarRating value)
     *   reviewCount    → product.reviewCount      (review count badge)
     *   inStock        → product.inStock          ("In Stock" / "Currently unavailable")
     */
    @Data
    public static class Response {
        private String  id;
        private String  name;
        private String  category;
        private String  subcategory;
        private String  brand;
        private double  price;
        private double  originalPrice;
        private int     stock;
        private String  imageUrl;
        private String  description;
        private boolean featured;

        /** Dynamically computed average from the reviews collection (0.0 when no reviews). */
        private Double  averageRating;

        /**
         * Total number of reviews.
         * Field is named "reviewCount" (not "totalRatings") to match
         * what ProductInfo.jsx reads as product.reviewCount.
         */
        private Integer reviewCount;

        /**
         * Whether this product is available for purchase.
         * Returned from Commit 2 onwards — managed by InventoryService.
         */
        private boolean inStock;
    }

    /** Used when UPDATING a product (PUT /api/products/{id}) */
    @Data
    public static class UpdateRequest {
        private String  name;
        private String  category;
        private String  subcategory;
        private String  brand;
        private double  price;
        private double  originalPrice;
        private int     stock;
        private String  imageUrl;
        private String  description;
        private boolean featured;
    }
}