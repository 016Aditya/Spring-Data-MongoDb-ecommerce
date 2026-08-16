package learnMongoDb.learnSpringMongoDb.service;

import learnMongoDb.learnSpringMongoDb.dto.WishlistDto;
import learnMongoDb.learnSpringMongoDb.dto.WishlistSyncRequest;
import learnMongoDb.learnSpringMongoDb.entity.Product;
import learnMongoDb.learnSpringMongoDb.entity.Wishlist;
import learnMongoDb.learnSpringMongoDb.repository.ProductRepository;
import learnMongoDb.learnSpringMongoDb.repository.WishlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WishlistService {

    @Autowired private WishlistRepository wishlistRepository;
    @Autowired private ProductRepository  productRepository;

    private Wishlist getOrCreate(String userId) {
        return wishlistRepository.findByUserId(userId)
                .orElseGet(() -> wishlistRepository.save(new Wishlist(userId)));
    }

    public WishlistDto getWishlistDtoByUserId(String userId) {
        return toDto(getOrCreate(userId));
    }

    public WishlistDto addProductToWishlist(String userId, String productId) {
        Wishlist w = getOrCreate(userId);
        if (!w.getProductIds().contains(productId)) {
            w.getProductIds().add(productId);
            wishlistRepository.save(w);
        }
        return toDto(w);
    }

    public WishlistDto removeProductFromWishlist(String userId, String productId) {
        Wishlist w = getOrCreate(userId);
        w.getProductIds().remove(productId);
        wishlistRepository.save(w);
        return toDto(w);
    }

    public void clearWishlist(String userId) {
        wishlistRepository.findByUserId(userId).ifPresent(w -> {
            w.getProductIds().clear();
            wishlistRepository.save(w);
        });
    }

    /**
     * syncGuestWishlist
     *
     * Merges a list of product IDs (saved by the frontend in LocalStorage
     * while the user browsed as a guest) into the authenticated user's
     * MongoDB wishlist in a single operation.
     *
     * Duplicate product IDs are silently skipped — the operation is
     * idempotent and safe to retry on network failure.
     *
     * Called via: POST /api/wishlist/user/{userId}/sync
     *
     * @param userId     The authenticated user's ID (from the URL path)
     * @param request    DTO containing the list of guest product IDs
     * @return           The updated WishlistDto (same shape as other endpoints)
     */
    public WishlistDto syncGuestWishlist(String userId, WishlistSyncRequest request) {
        if (request == null || request.getProductIds() == null || request.getProductIds().isEmpty()) {
            return toDto(getOrCreate(userId));
        }

        Wishlist w = getOrCreate(userId);
        boolean modified = false;

        for (String productId : request.getProductIds()) {
            if (productId != null && !productId.isBlank() && !w.getProductIds().contains(productId)) {
                w.getProductIds().add(productId);
                modified = true;
            }
        }

        if (modified) {
            wishlistRepository.save(w);
        }

        return toDto(w);
    }

    private WishlistDto toDto(Wishlist wishlist) {
        WishlistDto dto = new WishlistDto();
        dto.setId(wishlist.getId());
        dto.setUserId(wishlist.getUserId());

        List<Product> products =
                (List<Product>) productRepository.findAllById(wishlist.getProductIds());

        List<WishlistDto.WishlistItem> items = products.stream().map(p -> {
            WishlistDto.WishlistItem item = new WishlistDto.WishlistItem();
            item.setProductId(p.getId());
            item.setName(p.getName());
            item.setPrice(p.getPrice());
            item.setImageUrl(p.getImageUrl());
            item.setBrand(p.getBrand());
            item.setCategory(p.getCategory());
            return item;
        }).collect(Collectors.toList());

        dto.setItems(items);
        return dto;
    }
}