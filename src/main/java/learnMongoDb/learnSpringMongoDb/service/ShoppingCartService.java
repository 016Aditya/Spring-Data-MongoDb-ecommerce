package learnMongoDb.learnSpringMongoDb.service;

import learnMongoDb.learnSpringMongoDb.entity.CartItem;
import learnMongoDb.learnSpringMongoDb.entity.ShoppingCart;
import learnMongoDb.learnSpringMongoDb.repository.ShoppingCartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShoppingCartService {

    private final ShoppingCartRepository cartRepository;

    // ── GET OR CREATE CART ─────────────────────────────────────────────

    public ShoppingCart getCartByUserId(String userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> createEmptyCart(userId));
    }

    private ShoppingCart createEmptyCart(String userId) {
        ShoppingCart newCart = ShoppingCart.builder()
                .userId(userId)
                .items(new ArrayList<>())
                .cartTotal(0.0)
                .build();

        log.info("Created new empty cart for user: {}", userId);
        return cartRepository.save(newCart);
    }

    // ── ADD ITEM ───────────────────────────────────────────────────────

    public ShoppingCart addItemToCart(String userId, CartItem newItem) {
        ShoppingCart cart = getCartByUserId(userId);

        // Check if item already exists in the cart to update quantity instead of duplicating
        boolean itemExists = false;
        for (CartItem item : cart.getItems()) {
            if (item.getProductId().equals(newItem.getProductId())) {
                item.setQuantity(item.getQuantity() + newItem.getQuantity());
                itemExists = true;
                break;
            }
        }

        // If it's a completely new product, add it to the list
        if (!itemExists) {
            cart.getItems().add(newItem);
        }

        recalculateTotal(cart);
        log.info("Added item {} to cart for user: {}", newItem.getProductId(), userId);

        return cartRepository.save(cart);
    }

    // ── UPDATE ITEM QUANTITY ───────────────────────────────────────────

    public ShoppingCart updateItemQuantity(String userId, String productId, int newQuantity) {
        ShoppingCart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));

        if (newQuantity <= 0) {
            // If quantity is 0 or less, remove the item from the list completely
            cart.getItems().removeIf(item -> item.getProductId().equals(productId));
            log.info("Removed item {} from cart for user: {} due to zero quantity", productId, userId);
        } else {
            // Find the item and update its quantity
            cart.getItems().stream()
                    .filter(item -> item.getProductId().equals(productId))
                    .findFirst()
                    .ifPresentOrElse(
                            item -> item.setQuantity(newQuantity),
                            () -> { throw new RuntimeException("Item not found in cart!"); }
                    );
        }

        recalculateTotal(cart);
        return cartRepository.save(cart);
    }

    // ── REMOVE ITEM ────────────────────────────────────────────────────

    public ShoppingCart removeItemFromCart(String userId, String productId) {
        ShoppingCart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));

        // Defensive check just in case the cart is totally empty
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is already empty!");
        }

        // removeIf returns 'true' if it successfully found and removed the item
        boolean itemRemoved = cart.getItems().removeIf(item -> item.getProductId().equals(productId));

        if (!itemRemoved) {
            throw new RuntimeException("Item not found in cart!");
        }

        recalculateTotal(cart);
        log.info("Removed item {} from cart for user: {}", productId, userId);

        return cartRepository.save(cart);
    }

    // ── CLEAR CART ─────────────────────────────────────────────────────

    public void clearCart(String userId) {
        ShoppingCart cart = getCartByUserId(userId);
        cart.getItems().clear();
        cart.setCartTotal(0.0);
        cartRepository.save(cart);

        log.info("Cleared cart for user: {}", userId);
    }

    // ── PRIVATE HELPERS ────────────────────────────────────────────────

    /**
     * Helper method to keep total calculation DRY (Don't Repeat Yourself).
     */
    private void recalculateTotal(ShoppingCart cart) {
        double total = cart.getItems().stream()
                .mapToDouble(item -> item.getUnitPrice() * item.getQuantity())
                .sum();

        cart.setCartTotal(total);
    }
}