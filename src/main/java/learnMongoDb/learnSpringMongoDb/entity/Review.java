package learnMongoDb.learnSpringMongoDb.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@Document(collection = "reviews")
public class Review {

    @Id
    private String id;

    @Indexed
    private String productId; // Quickly find all reviews for a specific product

    @Indexed
    private String userId; // Quickly find all reviews written by a specific user

    private Integer rating; // e.g., 1 to 5 scale
    private String comment;

    @CreatedDate
    private LocalDateTime createdAt;
}