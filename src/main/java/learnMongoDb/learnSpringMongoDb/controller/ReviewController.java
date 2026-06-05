package learnMongoDb.learnSpringMongoDb.controller;

import learnMongoDb.learnSpringMongoDb.dto.ReviewDto;
import learnMongoDb.learnSpringMongoDb.entity.Review;
import learnMongoDb.learnSpringMongoDb.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewDto.Response> addReview(@RequestBody ReviewDto.Request request) {
        // Map DTO to Entity
        Review reviewToSave = Review.builder()
                .productId(request.getProductId())
                .userId(request.getUserId())
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        Review savedReview = reviewService.addReview(reviewToSave);
        return ResponseEntity.ok(mapToResponse(savedReview));
    }

    // --- REMOVE THIS OLD GET MAPPING ---
    // @GetMapping("/product/{productId}")
    // public ResponseEntity<List<ReviewDto.Response>> getReviewsByProduct(@PathVariable String productId) { ... }

    // --- ADD THIS NEW POST MAPPING ---
    @PostMapping("/search")
    public ResponseEntity<List<ReviewDto.Response>> getReviewsByProduct(@RequestBody ReviewDto.ProductSearchRequest request) {

        // Notice we are now grabbing the ID from the request body instead of the URL
        List<ReviewDto.Response> responses = reviewService.getReviewsByProduct(request.getProductId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewDto.Response> updateReview(
            @PathVariable String id,
            @RequestBody ReviewDto.UpdateRequest request) {

        // Pass the ID from the URL and the data from the Body to the Service
        Review updatedReview = reviewService.updateReview(id, request.getRating(), request.getComment());

        // Return the clean Response DTO
        return ResponseEntity.ok(mapToResponse(updatedReview));
    }

    // --- Helper Method ---
    private ReviewDto.Response mapToResponse(Review review) {
        ReviewDto.Response response = new ReviewDto.Response();
        response.setId(review.getId());
        response.setProductId(review.getProductId());
        response.setUserId(review.getUserId());
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setCreatedAt(review.getCreatedAt());
        return response;
    }
}