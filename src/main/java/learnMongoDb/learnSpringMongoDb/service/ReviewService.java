package learnMongoDb.learnSpringMongoDb.service;

import learnMongoDb.learnSpringMongoDb.entity.Review;
import learnMongoDb.learnSpringMongoDb.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public Review addReview(Review review) {
        // You can add validation logic here (e.g., ensuring rating is between 1 and 5)
        if (review.getRating() < 1 || review.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }
        return reviewRepository.save(review);
    }

    public List<Review> getReviewsByProduct(String productId) {
        return reviewRepository.findByProductId(productId);
    }

    public Review updateReview(String id, Integer rating, String comment) {
        // 1. Find the existing review
        Review existingReview = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found with ID: " + id));

        // 2. Update only the allowed fields
        existingReview.setRating(rating);
        existingReview.setComment(comment);

        // 3. Save and return (MongoDB updates the timestamp automatically)
        return reviewRepository.save(existingReview);
    }

    public void deleteReview(String id) {
        reviewRepository.deleteById(id);
    }
}