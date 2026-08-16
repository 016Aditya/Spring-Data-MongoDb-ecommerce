package learnMongoDb.learnSpringMongoDb.controller;

import jakarta.validation.Valid;
import learnMongoDb.learnSpringMongoDb.dto.CartDto;
import learnMongoDb.learnSpringMongoDb.entity.CartItem;
import learnMongoDb.learnSpringMongoDb.entity.Product;
import learnMongoDb.learnSpringMongoDb.entity.ShoppingCart;
import learnMongoDb.learnSpringMongoDb.security.CustomUserDetails;
import learnMongoDb.learnSpringMongoDb.service.ProductService;
import learnMongoDb.learnSpringMongoDb.service.ShoppingCartService;
import learnMongoDb.learnSpringMongoDb.service.sync.CartSyncService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class ShoppingCartController {

    private final ShoppingCartService cartService;
    private final CartSyncService     cartSyncService;
    private final ProductService      productService;
    private final ModelMapper         modelMapper;

    // ── GET CART ───────────────────────────────────────────────────────

    @GetMapping("/{userId}")
    public ResponseEntity<?> getCart(
            @PathVariable String userId,
            @AuthenticationPrincipal CustomUserDetails principal) {

        if (!principal.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Forbidden"));
        }

        ShoppingCart cart = cartService.getCartByUserId(userId);
        return ResponseEntity.ok(modelMapper.map(cart, CartDto.Response.class));
    }

    // ── ADD ITEM ───────────────────────────────────────────────────────

    @PostMapping("/{userId}/add")
    public ResponseEntity<?> addItemToCart(
            @PathVariable String userId,
            @RequestBody CartDto.AddItemRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {

        if (!principal.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Forbidden"));
        }

        Product product = productService.getProductById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found!"));

        CartItem item = CartItem.builder()
                .productId(product.getId())
                .quantity(request.getQuantity())
                .unitPrice(product.getPrice())
                .build();

        ShoppingCart updatedCart = cartService.addItemToCart(userId, item);
        return ResponseEntity.ok(modelMapper.map(updatedCart, CartDto.Response.class));
    }

    // ── UPDATE ITEM QUANTITY ───────────────────────────────────────────

    @PutMapping("/{userId}/items")
    public ResponseEntity<?> updateCartItem(
            @PathVariable String userId,
            @RequestBody CartDto.UpdateItemRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {

        if (!principal.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Forbidden"));
        }

        ShoppingCart updatedCart = cartService.updateItemQuantity(
                userId,
                request.getProductId(),
                request.getQuantity()
        );

        return ResponseEntity.ok(modelMapper.map(updatedCart, CartDto.Response.class));
    }

    // ── REMOVE SPECIFIC ITEM ───────────────────────────────────────────

    @DeleteMapping("/{userId}/items/{productId}")
    public ResponseEntity<?> removeItemFromCart(
            @PathVariable String userId,
            @PathVariable String productId,
            @AuthenticationPrincipal CustomUserDetails principal) {

        if (!principal.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Forbidden"));
        }

        ShoppingCart updatedCart = cartService.removeItemFromCart(userId, productId);
        return ResponseEntity.ok(modelMapper.map(updatedCart, CartDto.Response.class));
    }

    // ── CLEAR ENTIRE CART ──────────────────────────────────────────────

    @DeleteMapping("/{userId}/clear")
    public ResponseEntity<?> clearCart(
            @PathVariable String userId,
            @AuthenticationPrincipal CustomUserDetails principal) {

        if (!principal.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Forbidden"));
        }

        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

    // ── SYNC GUEST CART ────────────────────────────────────────────────

    /**
     * Merges a guest LocalStorage cart into the authenticated user's MongoDB cart.
     *
     * POST /api/cart/{userId}/sync
     *
     * Called once by the frontend after login/registration if the user had
     * items in their guest cart. Delegates all merge logic to CartSyncService.
     * Returns the fully merged cart using the same CartDto.Response shape
     * as every other cart endpoint so the frontend cache stays consistent.
     *
     * Request body:
     * {
     *   "items": [
     *     { "productId": "64b123", "quantity": 2 },
     *     { "productId": "64c888", "quantity": 1 }
     *   ]
     * }
     */
    @PostMapping("/{userId}/sync")
    public ResponseEntity<?> syncGuestCart(
            @PathVariable String userId,
            @Valid @RequestBody CartDto.SyncRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {

        if (!principal.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Forbidden"));
        }

        ShoppingCart mergedCart = cartSyncService.syncGuestCart(userId, request);
        return ResponseEntity.ok(modelMapper.map(mergedCart, CartDto.Response.class));
    }
}