package learnMongoDb.learnSpringMongoDb.service;

import learnMongoDb.learnSpringMongoDb.dto.ReviewDto;
import learnMongoDb.learnSpringMongoDb.entity.Product;
import learnMongoDb.learnSpringMongoDb.entity.Review;
import learnMongoDb.learnSpringMongoDb.repository.ProductRepository;
import learnMongoDb.learnSpringMongoDb.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    // ─── Add ─────────────────────────────────────────────────────────────────

    public Review addReview(Review review) {
        review.setCreatedAt(LocalDateTime.now());
        Review saved = reviewRepository.save(review);
        recalculateProductRating(review.getProductId());
        return saved;
    }

    // ─── Read ─────────────────────────────────────────────────────────────────

    public List<Review> getReviewsByProduct(String productId) {
        return reviewRepository.findByProductId(productId);
    }

    // ─── Update ───────────────────────────────────────────────────────────────

    public Review updateReview(String reviewId, int rating, String comment, String currentUserId) {
        Review existing = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + reviewId));

        if (!existing.getUserId().equals(currentUserId)) {
            throw new RuntimeException("Forbidden: You can only edit your own reviews.");
        }

        existing.setRating(rating);
        existing.setComment(comment);
        Review updated = reviewRepository.save(existing);

        // Rating value changed — recalculate average
        recalculateProductRating(existing.getProductId());
        return updated;
    }

    // ─── Delete ───────────────────────────────────────────────────────────────

    public void deleteReview(String reviewId, String currentUserId) {
        Review existing = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + reviewId));

        if (!existing.getUserId().equals(currentUserId)) {
            throw new RuntimeException("Forbidden: You can only delete your own reviews.");
        }

        String productId = existing.getProductId();
        reviewRepository.deleteById(reviewId);

        // Review removed — recalculate (count drops by 1, average may change)
        recalculateProductRating(productId);
    }

    // ─── Private helper ──────────────────────────────────────────────────────

    /**
     * Recomputes averageRating (rounded to 1 decimal) and reviewCount
     * from the reviews collection and persists them on the Product document.
     *
     * Called after every add / update / delete so the product page always
     * shows real-time values without a separate admin job.
     */
    private void recalculateProductRating(String productId) {
        Product product = productRepository.findById(productId)
                .orElse(null);
        if (product == null) return;

        List<Review> reviews = reviewRepository.findRatingsByProductId(productId);
        int count = reviews.size();

        if (count == 0) {
            product.setAverageRating(0.0);
            product.setTotalRatings(0);
        } else {
            double sum = reviews.stream()
                    .mapToInt(r -> r.getRating() != null ? r.getRating() : 0)
                    .sum();
            // Round to 1 decimal place: e.g. 4.333... → 4.3
            double avg = Math.round((sum / count) * 10.0) / 10.0;
            product.setAverageRating(avg);
            product.setTotalRatings(count);
        }

        productRepository.save(product);
    }
}