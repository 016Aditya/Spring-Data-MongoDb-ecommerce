package learnMongoDb.learnSpringMongoDb.service;

import learnMongoDb.learnSpringMongoDb.dto.WishlistDto;
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

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private ProductRepository productRepository; // Needed to fetch product details

    // 1. Core entity fetcher
    private Wishlist getWishlistEntity(String userId) {
        return wishlistRepository.findByUserId(userId)
                .orElseGet(() -> wishlistRepository.save(new Wishlist(userId)));
    }

    // 2. Hydrate entity into DTO for the frontend
    public WishlistDto getWishlistDtoByUserId(String userId) {
        Wishlist wishlist = getWishlistEntity(userId);
        return convertToDto(wishlist);
    }

    public WishlistDto addProductToWishlist(String userId, String productId) {
        Wishlist wishlist = getWishlistEntity(userId);
        if (!wishlist.getProductIds().contains(productId)) {
            wishlist.getProductIds().add(productId);
            wishlistRepository.save(wishlist);
        }
        return convertToDto(wishlist); // Return populated DTO to frontend
    }

    public WishlistDto removeProductFromWishlist(String userId, String productId) {
        Wishlist wishlist = getWishlistEntity(userId);
        if (wishlist.getProductIds().contains(productId)) {
            wishlist.getProductIds().remove(productId);
            wishlistRepository.save(wishlist);
        }
        return convertToDto(wishlist); // Return populated DTO to frontend
    }

    public void clearWishlist(String userId) {
        wishlistRepository.findByUserId(userId).ifPresent(wishlist -> {
            wishlist.getProductIds().clear();
            wishlistRepository.save(wishlist);
        });
    }

    // --- Helper Method ---
    // Converts the raw entity to a populated DTO
    private WishlistDto convertToDto(Wishlist wishlist) {
        WishlistDto dto = new WishlistDto();
        dto.setId(wishlist.getId());
        dto.setUserId(wishlist.getUserId());

        // Fetch all products that match the IDs in the wishlist
        List<Product> products = (List<Product>) productRepository.findAllById(wishlist.getProductIds());

        // Map the Product entities to WishlistItem DTOs
        List<WishlistDto.WishlistItem> items = products.stream().map(product -> {
            WishlistDto.WishlistItem item = new WishlistDto.WishlistItem();
            item.setProductId(product.getId());
            item.setName(product.getName());
            item.setPrice(product.getPrice());

            // NOTE: Adjust "getImageUrl()" to match whatever your Product entity uses (e.g., getImages().get(0))
            item.setImageUrl(product.getImageUrl());

            return item;
        }).collect(Collectors.toList());

        dto.setItems(items);
        return dto;
    }
}