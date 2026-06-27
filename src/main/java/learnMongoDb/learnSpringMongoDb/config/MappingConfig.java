package learnMongoDb.learnSpringMongoDb.config;

import learnMongoDb.learnSpringMongoDb.dto.CartDto;
import learnMongoDb.learnSpringMongoDb.dto.OrderDto;
import learnMongoDb.learnSpringMongoDb.dto.ProductDto;
import learnMongoDb.learnSpringMongoDb.dto.UserDto;
import learnMongoDb.learnSpringMongoDb.entity.*;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class MappingConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        // ── 1. Core Configuration ──
        modelMapper.getConfiguration()
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE)
                .setMatchingStrategy(MatchingStrategies.STRICT);

        // ── 2. OrderItem -> OrderItemResponse ──
        modelMapper.typeMap(OrderItem.class, OrderDto.OrderItemResponse.class).addMappings(mapper -> {
            mapper.map(OrderItem::getProductImage, OrderDto.OrderItemResponse::setImageUrl);
            mapper.map(OrderItem::getProductImage, OrderDto.OrderItemResponse::setProductImage);
        });

        // ── 3. Order -> OrderDto.Response (Legacy Items Fallback) ──
        Converter<Order, List<OrderDto.OrderItemResponse>> itemsFallbackConverter = context -> {
            Order order = context.getSource();
            List<OrderItem> rawItems = order.getItems();

            if (rawItems == null || rawItems.isEmpty()) {
                rawItems = order.getLegacyProducts();
            }
            if (rawItems == null) {
                rawItems = Collections.emptyList();
            }

            return rawItems.stream()
                    .map(item -> modelMapper.map(item, OrderDto.OrderItemResponse.class))
                    .collect(Collectors.toList());
        };

        modelMapper.typeMap(Order.class, OrderDto.Response.class).addMappings(mapper -> {
            mapper.using(itemsFallbackConverter).map(src -> src, OrderDto.Response::setItems);
        });

        // ── 4. Product -> ProductDto.Response ──
        Converter<Integer, Boolean> stockToBooleanConverter = context ->
                context.getSource() != null && context.getSource() > 0;

        modelMapper.typeMap(Product.class, ProductDto.Response.class).addMappings(mapper -> {
            mapper.map(Product::getTotalRatings, ProductDto.Response::setReviewCount);
            mapper.using(stockToBooleanConverter).map(Product::getStock, ProductDto.Response::setInStock);
        });

// ── 5. Custom Mapping for ShoppingCart -> CartDto.Response ──

        // Step 1: Explicitly register the mapping for the embedded list items
        modelMapper.typeMap(CartItem.class, CartDto.CartItemResponse.class).addMappings(mapper -> {
            mapper.map(CartItem::getProductId, CartDto.CartItemResponse::setProductId);
            mapper.map(CartItem::getQuantity, CartDto.CartItemResponse::setQuantity);
            mapper.map(CartItem::getUnitPrice, CartDto.CartItemResponse::setUnitPrice);
        });

        // Step 2: Map the parent Cart and tell it to map the list
        modelMapper.typeMap(ShoppingCart.class, CartDto.Response.class).addMappings(mapper -> {
            mapper.map(ShoppingCart::getId, CartDto.Response::setId);
            mapper.map(ShoppingCart::getUserId, CartDto.Response::setUserId);
            mapper.map(ShoppingCart::getCartTotal, CartDto.Response::setCartTotal);
            mapper.map(ShoppingCart::getItems, CartDto.Response::setItems);
        });
// ── 6. Custom Mapping for User <-> UserDto ──

        // Pass-through converter: Safely passes the Instant directly without trying to modify it
        Converter<java.time.Instant, java.time.Instant> instantPassThroughConverter = context ->
                context.getSource();

        // Map Entity to Response DTO
        modelMapper.typeMap(User.class, UserDto.Response.class).addMappings(mapper -> {
            mapper.using(instantPassThroughConverter).map(User::getCreatedAt, UserDto.Response::setCreatedAt);

            // If you also have an updatedAt field, map it the exact same way:
            // mapper.using(instantPassThroughConverter).map(User::getUpdatedAt, UserDto.Response::setUpdatedAt);
        });

        // Map Request DTO to Entity (handling the password field name mismatch)
        modelMapper.typeMap(UserDto.Request.class, User.class).addMappings(mapper -> {
            mapper.map(UserDto.Request::getPassword, User::setPasswordHash);
        });

        return modelMapper;    }
}