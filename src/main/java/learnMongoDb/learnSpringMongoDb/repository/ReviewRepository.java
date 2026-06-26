package learnMongoDb.learnSpringMongoDb.repository;

import learnMongoDb.learnSpringMongoDb.entity.Review;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {

    /** All reviews for a product (used by ReviewService.getReviewsByProduct). */
    List<Review> findByProductId(String productId);

    /** Total review count for a product — used to update Product.reviewCount. */
    long countByProductId(String productId);

    /**
     * Returns just the rating values for a product so we can
     * compute the average without loading full Review documents.
     */
    @Query(value = "{ 'productId': ?0 }", fields = "{ 'rating': 1, '_id': 0 }")
    List<Review> findRatingsByProductId(String productId);
}