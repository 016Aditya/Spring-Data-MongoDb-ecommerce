package learnMongoDb.learnSpringMongoDb.dto;

import lombok.Data;

public class ProductDto {

    @Data
    public static class Request {
        private String name;
        private String category;
        private double price;
        private String imageUrl;
        private String description;
    }

    @Data
    public static class Response {
        private String id;
        private String name;
        private String category;
        private double price;
        private String imageUrl;
        private String description;
    }

    @Data
    public static class UpdateRequest {
        private String name;
        private String category;
        private double price;
        private String imageUrl;
        private String description;
    }
}
