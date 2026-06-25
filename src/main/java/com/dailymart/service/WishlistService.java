package com.dailymart.service;

import com.dailymart.dto.*;
import com.dailymart.entity.*;
import com.dailymart.exception.*;
import com.dailymart.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public List<WishlistDto> getWishlist(String email) {
        User user = getUser(email);
        return wishlistRepository.findByUserId(user.getId()).stream()
            .map(w -> WishlistDto.builder().id(w.getId())
                .addedAt(w.getAddedAt()).product(toProductDto(w.getProduct())).build())
            .toList();
    }

    @Transactional
    public MessageResponse toggleWishlist(Long productId, String email) {
        User user = getUser(email);
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        if (wishlistRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            wishlistRepository.deleteByUserIdAndProductId(user.getId(), productId);
            return new MessageResponse("Removed from wishlist");
        } else {
            wishlistRepository.save(Wishlist.builder().user(user).product(product).build());
            return new MessageResponse("Added to wishlist");
        }
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private ProductDto toProductDto(Product p) {
        return ProductDto.builder()
            .id(p.getId()).name(p.getName()).brand(p.getBrand())
            .sellingPrice(p.getSellingPrice()).originalPrice(p.getOriginalPrice())
            .discountPercent(p.getDiscountPercent()).averageRating(p.getAverageRating()).build();
    }
}
