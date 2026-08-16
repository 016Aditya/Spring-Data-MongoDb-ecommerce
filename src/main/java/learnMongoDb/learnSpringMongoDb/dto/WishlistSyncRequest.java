package learnMongoDb.learnSpringMongoDb.dto;

import java.util.List;

/**
 * WishlistSyncRequest
 *
 * Request body for the guest wishlist bulk-sync endpoint.
 *
 * POST /api/wishlist/user/{userId}/sync
 *
 * The frontend sends the product IDs that were saved in LocalStorage
 * while the user was browsing as a guest. The backend merges them into
 * the authenticated user's wishlist, skipping any duplicates.
 *
 * Example JSON:
 * {
 *   "productIds": ["64b123", "64b555", "64c888"]
 * }
 */
public class WishlistSyncRequest {

    private List<String> productIds;

    // ── No-arg constructor (required by Jackson) ─────────────────────────
    public WishlistSyncRequest() {}

    public WishlistSyncRequest(List<String> productIds) {
        this.productIds = productIds;
    }

    public List<String> getProductIds() {
        return productIds;
    }

    public void setProductIds(List<String> productIds) {
        this.productIds = productIds;
    }
}