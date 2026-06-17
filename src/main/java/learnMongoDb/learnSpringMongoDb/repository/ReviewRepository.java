package learnMongoDb.learnSpringMongoDb.repository;

import learnMongoDb.learnSpringMongoDb.entity.Review;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends MongoRepository<Review, String> {

    List<Review> findByProductId(String productId);

    Optional<Review> findByProductIdAndUserId(
            String productId,
            String userId
    );
}