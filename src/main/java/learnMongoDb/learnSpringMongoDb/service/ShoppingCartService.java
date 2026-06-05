package learnMongoDb.learnSpringMongoDb.service;

import learnMongoDb.learnSpringMongoDb.entity.CartItem;
import learnMongoDb.learnSpringMongoDb.entity.ShoppingCart;
import learnMongoDb.learnSpringMongoDb.repository.ShoppingCartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class ShoppingCartService {

    private final ShoppingCartRepository cartRepository;

    // Fetches the cart, or creates a new one if it doesn't exist
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
        return cartRepository.save(newCart);
    }

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

        // Recalculate total price of the cart
        double newTotal = cart.getItems().stream()
                .mapToDouble(item -> item.getQuantity() * item.getUnitPrice())
                .sum();

        cart.setCartTotal(newTotal);

        return cartRepository.save(cart);
    }

    public void clearCart(String userId) {
        ShoppingCart cart = getCartByUserId(userId);
        cart.getItems().clear();
        cart.setCartTotal(0.0);
        cartRepository.save(cart);
    }

    public ShoppingCart updateItemQuantity(String userId, String productId, int newQuantity) {
        ShoppingCart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));

        if (newQuantity <= 0) {
            // If quantity is 0 or less, remove the item from the list completely
            cart.getItems().removeIf(item -> item.getProductId().equals(productId));
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

        // Recalculate the total price of the cart
        // Recalculate the total price of the cart
        double total = cart.getItems().stream()
                .mapToDouble(item -> item.getUnitPrice() * item.getQuantity())
                .sum();

        // CHANGE THIS LINE:
        // cart.setTotalPrice(total);

        // TO THIS:
        cart.setCartTotal(total);

        return cartRepository.save(cart);
    }
}