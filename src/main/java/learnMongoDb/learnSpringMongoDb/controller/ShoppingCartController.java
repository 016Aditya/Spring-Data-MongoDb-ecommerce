package learnMongoDb.learnSpringMongoDb.controller;

import learnMongoDb.learnSpringMongoDb.dto.CartDto;
import learnMongoDb.learnSpringMongoDb.entity.CartItem;
import learnMongoDb.learnSpringMongoDb.entity.Product;
import learnMongoDb.learnSpringMongoDb.entity.ShoppingCart;
import learnMongoDb.learnSpringMongoDb.service.ProductService;
import learnMongoDb.learnSpringMongoDb.service.ShoppingCartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class ShoppingCartController {

    private final ShoppingCartService cartService;
    private final ProductService productService; // Added to securely fetch prices

    @GetMapping("/{userId}")
    public ResponseEntity<ShoppingCart> getCart(@PathVariable String userId) {
        return ResponseEntity.ok(cartService.getCartByUserId(userId));
    }

    @PostMapping("/{userId}/add")
    public ResponseEntity<ShoppingCart> addItemToCart(
            @PathVariable String userId,
            @RequestBody CartDto.AddItemRequest request) {

        // 1. Securely fetch the real product from the DB to get the true price
        Product product = productService.getProductById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found!"));

        // 2. Build the CartItem using the client's quantity and the server's price
        CartItem item = CartItem.builder()
                .productId(product.getId())
                .quantity(request.getQuantity())
                .unitPrice(product.getPrice())
                .build();

        // 3. Save and return
        return ResponseEntity.ok(cartService.addItemToCart(userId, item));
    }

    @DeleteMapping("/{userId}/clear")
    public ResponseEntity<Void> clearCart(@PathVariable String userId) {
        cartService.clearCart(userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{userId}/items")
    public ResponseEntity<ShoppingCart> updateCartItem(
            @PathVariable String userId,
            @RequestBody CartDto.UpdateItemRequest request) {

        ShoppingCart updatedCart = cartService.updateItemQuantity(
                userId,
                request.getProductId(),
                request.getQuantity()
        );

        return ResponseEntity.ok(updatedCart);
    }

    @DeleteMapping("/{userId}/items/{productId}")
    public ResponseEntity<ShoppingCart> removeItemFromCart(
            @PathVariable String userId,
            @PathVariable String productId) {

        ShoppingCart updatedCart = cartService.removeItemFromCart(userId, productId);

        return ResponseEntity.ok(updatedCart);
    }
}