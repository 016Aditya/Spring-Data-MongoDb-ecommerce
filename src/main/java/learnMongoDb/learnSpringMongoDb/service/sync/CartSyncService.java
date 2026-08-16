package learnMongoDb.learnSpringMongoDb.service.sync;

import learnMongoDb.learnSpringMongoDb.dto.CartDto;
import learnMongoDb.learnSpringMongoDb.entity.CartItem;
import learnMongoDb.learnSpringMongoDb.entity.Product;
import learnMongoDb.learnSpringMongoDb.entity.ShoppingCart;
import learnMongoDb.learnSpringMongoDb.error.ResourceNotFoundException;
import learnMongoDb.learnSpringMongoDb.repository.ShoppingCartRepository;
import learnMongoDb.learnSpringMongoDb.service.ProductService;
import learnMongoDb.learnSpringMongoDb.service.ShoppingCartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CartSyncService
 *
 * Handles guest-cart-to-user-cart synchronisation.
 *
 * Responsibility split:
 *   - ShoppingCartService  → normal cart domain operations (add, remove, update, clear, total)
 *   - CartSyncService      → one-time post-login merge of a guest LocalStorage cart into
 *                            the authenticated user's MongoDB cart
 *
 * Merge rules:
 *   1. Aggregate duplicate productIds from the guest payload before touching the DB.
 *   2. Load (or create) the authenticated user's cart once.
 *   3. For every aggregated guest entry:
 *        - if the product already exists in the cart  → sum quantities
 *        - if it is a new product                     → add a new CartItem with backend price
 *   4. Recalculate totals once after all merges.
 *   5. Save the cart exactly once.
 *
 * An empty item list is a valid no-op; the current cart is returned unchanged.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartSyncService {

    private final ShoppingCartRepository   cartRepository;
    private final ShoppingCartService      cartService;    // reused for getCartByUserId + recalculation
    private final ProductService           productService;

    // ── PUBLIC ENTRY POINT ────────────────────────────────────────────

    /**
     * Merges the guest cart payload into the authenticated user's cart.
     *
     * @param userId  authenticated user ID resolved from JWT
     * @param request validated sync request body from the controller
     * @return the fully merged, saved ShoppingCart
     */
    public ShoppingCart syncGuestCart(String userId, CartDto.SyncRequest request) {

        List<CartDto.CartItemRequest> guestItems = request.getItems();

        // Empty payload → no-op, return current cart (create if missing)
        if (guestItems == null || guestItems.isEmpty()) {
            log.info("Sync called with empty payload for user: {} — returning current cart", userId);
            return cartService.getCartByUserId(userId);
        }

        // Step 1: aggregate duplicate guest entries by productId
        Map<String, Integer> aggregated = aggregateGuestItems(guestItems);
        log.info("Syncing {} unique product(s) for user: {}", aggregated.size(), userId);

        // Step 2: load or create the authenticated user's cart
        ShoppingCart cart = cartService.getCartByUserId(userId);

        if (cart.getItems() == null) {
            cart.setItems(new ArrayList<>());
        }

        // Step 3: merge each aggregated entry
        for (Map.Entry<String, Integer> entry : aggregated.entrySet()) {
            String  productId     = entry.getKey();
            int     guestQuantity = entry.getValue();

            // Validate product exists and fetch authoritative price
            Product product = productService.getProductById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found during cart sync: " + productId));

            mergeItem(cart, product, guestQuantity);
        }

        // Step 4: recalculate total once after all items are merged
        recalculateTotal(cart);

        // Step 5: persist once
        ShoppingCart saved = cartRepository.save(cart);
        log.info("Cart sync complete for user: {} — {} item(s), total: {}",
                userId, saved.getItems().size(), saved.getCartTotal());

        return saved;
    }

    // ── PRIVATE HELPERS ────────────────────────────────────────────────

    /**
     * Collapses duplicate productIds in the guest payload into a single
     * productId → totalQuantity map before touching the cart.
     */
    private Map<String, Integer> aggregateGuestItems(List<CartDto.CartItemRequest> guestItems) {
        Map<String, Integer> aggregated = new HashMap<>();
        for (CartDto.CartItemRequest item : guestItems) {
            aggregated.merge(item.getProductId(), item.getQuantity(), Integer::sum);
        }
        return aggregated;
    }

    /**
     * Merges a single validated product into the cart.
     * If the product already exists, quantities are summed.
     * If it is new, a fresh CartItem is appended with the backend price.
     */
    private void mergeItem(ShoppingCart cart, Product product, int guestQuantity) {
        boolean found = false;

        for (CartItem item : cart.getItems()) {
            if (item.getProductId().equals(product.getId())) {
                item.setQuantity(item.getQuantity() + guestQuantity);
                found = true;
                break;
            }
        }

        if (!found) {
            cart.getItems().add(
                    CartItem.builder()
                            .productId(product.getId())
                            .quantity(guestQuantity)
                            .unitPrice(product.getPrice())  // authoritative backend price
                            .build()
            );
        }
    }

    /**
     * Recalculates cartTotal from current items.
     * Mirrors ShoppingCartService#recalculateTotal without duplicating
     * that method (which is private there). Kept here so CartSyncService
     * can recalculate once after the full merge loop without an extra save.
     */
    private void recalculateTotal(ShoppingCart cart) {
        double total = cart.getItems().stream()
                .mapToDouble(item -> item.getUnitPrice() * item.getQuantity())
                .sum();
        cart.setCartTotal(total);
    }
}