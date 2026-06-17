package learnMongoDb.learnSpringMongoDb.service;

import learnMongoDb.learnSpringMongoDb.entity.Product;
import learnMongoDb.learnSpringMongoDb.entity.Review;
import learnMongoDb.learnSpringMongoDb.repository.ProductRepository;
import learnMongoDb.learnSpringMongoDb.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    public Review addReview(Review review) {

        if (review.getRating() < 1 ||
                review.getRating() > 5) {
            throw new IllegalArgumentException(
                    "Rating must be between 1 and 5."
            );
        }

        boolean alreadyReviewed =
                reviewRepository.findByProductIdAndUserId(
                        review.getProductId(),
                        review.getUserId()
                ).isPresent();

        if (alreadyReviewed) {
            throw new RuntimeException(
                    "You have already reviewed this product."
            );
        }

        Product product =
                productRepository.findById(
                        review.getProductId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Product not found."
                        ));

        Review savedReview =
                reviewRepository.save(review);

        updateProductRating(product.getId());

        return savedReview;
    }

    public List<Review> getReviewsByProduct(
            String productId
    ) {
        return reviewRepository.findByProductId(
                productId
        );
    }

    public Review updateReview(
            String id,
            Integer rating,
            String comment
    ) {

        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException(
                    "Rating must be between 1 and 5."
            );
        }

        Review existingReview =
                reviewRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Review not found with ID: " + id
                                ));

        existingReview.setRating(rating);
        existingReview.setComment(comment);

        Review updatedReview =
                reviewRepository.save(
                        existingReview
                );

        updateProductRating(
                updatedReview.getProductId()
        );

        return updatedReview;
    }

    public void deleteReview(String id) {

        Review review =
                reviewRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Review not found with ID: " + id
                                ));

        String productId =
                review.getProductId();

        reviewRepository.delete(review);

        updateProductRating(productId);
    }

    private void updateProductRating(
            String productId
    ) {

        Product product =
                productRepository.findById(productId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Product not found."
                                ));

        List<Review> reviews =
                reviewRepository.findByProductId(
                        productId
                );

        int totalRatings =
                reviews.size();

        double averageRating =
                reviews.stream()
                        .mapToInt(
                                Review::getRating
                        )
                        .average()
                        .orElse(0.0);

        averageRating =
                Math.round(
                        averageRating * 10.0
                ) / 10.0;

        product.setAverageRating(
                averageRating
        );

        product.setTotalRatings(
                totalRatings
        );

        productRepository.save(product);
    }
}