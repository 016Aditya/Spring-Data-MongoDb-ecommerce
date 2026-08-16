package learnMongoDb.learnSpringMongoDb.controller;

import learnMongoDb.learnSpringMongoDb.dto.WishlistDto;
import learnMongoDb.learnSpringMongoDb.dto.WishlistSyncRequest;
import learnMongoDb.learnSpringMongoDb.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<WishlistDto> getWishlist(@PathVariable String userId) {
        return ResponseEntity.ok(wishlistService.getWishlistDtoByUserId(userId));
    }

    @PostMapping("/user/{userId}/add/{productId}")
    public ResponseEntity<WishlistDto> addProduct(
            @PathVariable String userId,
            @PathVariable String productId) {
        return ResponseEntity.ok(wishlistService.addProductToWishlist(userId, productId));
    }

    @DeleteMapping("/user/{userId}/remove/{productId}")
    public ResponseEntity<WishlistDto> removeProduct(
            @PathVariable String userId,
            @PathVariable String productId) {
        return ResponseEntity.ok(wishlistService.removeProductFromWishlist(userId, productId));
    }

    @DeleteMapping("/user/{userId}/clear")
    public ResponseEntity<Void> clearWishlist(@PathVariable String userId) {
        wishlistService.clearWishlist(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Bulk sync endpoint — merges guest LocalStorage wishlist into MongoDB.
     *
     * POST /api/wishlist/user/{userId}/sync
     *
     * Called once after login/registration if the user had saved items as a
     * guest. Skips any product IDs already present in the wishlist.
     * Returns the complete updated wishlist so the frontend can refresh
     * its TanStack Query cache in a single round-trip.
     *
     * Request body: { "productIds": ["id1", "id2", ...] }
     */
    @PostMapping("/user/{userId}/sync")
    public ResponseEntity<WishlistDto> syncGuestWishlist(
            @PathVariable String userId,
            @RequestBody WishlistSyncRequest request) {
        return ResponseEntity.ok(wishlistService.syncGuestWishlist(userId, request));
    }
}