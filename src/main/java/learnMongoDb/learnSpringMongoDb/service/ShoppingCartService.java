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
}