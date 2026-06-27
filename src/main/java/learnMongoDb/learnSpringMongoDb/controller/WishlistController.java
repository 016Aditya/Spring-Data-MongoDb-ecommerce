package learnMongoDb.learnSpringMongoDb.controller;

import learnMongoDb.learnSpringMongoDb.dto.WishlistDto;
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
}