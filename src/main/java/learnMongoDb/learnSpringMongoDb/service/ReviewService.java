package learnMongoDb.learnSpringMongoDb.service;

import learnMongoDb.learnSpringMongoDb.dto.ReviewDto;
import learnMongoDb.learnSpringMongoDb.entity.Review;
import learnMongoDb.learnSpringMongoDb.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public Review addReview(Review review) {
        review.setCreatedAt(LocalDateTime.now());

        // Note: If you want userName and userEmail to be saved directly on the Review document,
        // you should fetch the User entity from UserRepository here and set those fields.

        return reviewRepository.save(review);
    }

    public List<Review> getReviewsByProduct(String productId) {
        return reviewRepository.findByProductId(productId);
    }

    public Review updateReview(String reviewId, int rating, String comment, String currentUserId) {
        Review existingReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + reviewId));

        // SECURE: Check if the logged-in user actually owns this review
        if (!existingReview.getUserId().equals(currentUserId)) {
            throw new RuntimeException("Forbidden: You can only edit your own reviews.");
        }

        existingReview.setRating(rating);
        existingReview.setComment(comment);

        return reviewRepository.save(existingReview);
    }

    public void deleteReview(String reviewId, String currentUserId) {
        Review existingReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + reviewId));

        // SECURE: Check if the logged-in user actually owns this review
        if (!existingReview.getUserId().equals(currentUserId)) {
            throw new RuntimeException("Forbidden: You can only delete your own reviews.");
        }

        reviewRepository.deleteById(reviewId);
    }

    public List<Review> searchReviews(ReviewDto.ProductSearchRequest request) {
        return reviewRepository.findByProductId(request.getProductId());
    }
}