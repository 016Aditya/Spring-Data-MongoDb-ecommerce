package learnMongoDb.learnSpringMongoDb.controller;

import learnMongoDb.learnSpringMongoDb.dto.ReviewDto;
import learnMongoDb.learnSpringMongoDb.entity.Review;
import learnMongoDb.learnSpringMongoDb.security.CustomUserDetails;
import learnMongoDb.learnSpringMongoDb.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final ModelMapper modelMapper;

    @PostMapping
    public ResponseEntity<ReviewDto.Response> addReview(
            @RequestBody ReviewDto.Request request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        Review reviewToSave = modelMapper.map(request, Review.class);

        // SECURE: Set userId strictly from the JWT
        reviewToSave.setUserId(currentUser.getUserId());

        Review savedReview = reviewService.addReview(reviewToSave);
        return ResponseEntity.ok(modelMapper.map(savedReview, ReviewDto.Response.class));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewDto.Response>> getReviewsByProduct(@PathVariable String productId) {
        List<ReviewDto.Response> responses = reviewService.getReviewsByProduct(productId)
                .stream()
                .map(review -> modelMapper.map(review, ReviewDto.Response.class))
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewDto.Response> updateReview(
            @PathVariable String id,
            @RequestBody ReviewDto.UpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Pass the JWT userId down to the service to verify ownership
        Review updatedReview = reviewService.updateReview(
                id,
                request.getRating(),
                request.getComment(),
                currentUser.getUserId()
        );

        return ResponseEntity.ok(modelMapper.map(updatedReview, ReviewDto.Response.class));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Pass the JWT userId down to the service to verify ownership
        reviewService.deleteReview(id, currentUser.getUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/search")
    public ResponseEntity<List<ReviewDto.Response>> searchReviewsByProduct(
            @RequestBody ReviewDto.ProductSearchRequest request) {

        List<ReviewDto.Response> responses = reviewService.searchReviews(request)
                .stream()
                .map(review -> modelMapper.map(review, ReviewDto.Response.class))
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }
}