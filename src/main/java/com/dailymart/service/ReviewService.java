package com.dailymart.service;

import com.dailymart.dto.*;
import com.dailymart.entity.*;
import com.dailymart.exception.*;
import com.dailymart.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public Page<ReviewDto> getProductReviews(Long productId, int page, int size) {
        return reviewRepository.findByProductIdAndApprovedTrue(
            productId, PageRequest.of(page, size, Sort.by("createdAt").descending()))
            .map(this::toDto);
    }

    @Transactional
    public ReviewDto addReview(Long productId, String email, CreateReviewRequest req) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (reviewRepository.existsByProductIdAndUserId(productId, user.getId()))
            throw new BadRequestException("You have already reviewed this product");

        boolean verified = false;
        if (req.getOrderId() != null) {
            verified = orderRepository.findById(req.getOrderId())
                .map(o -> o.getUser().getId().equals(user.getId()) &&
                    o.getOrderItems().stream()
                        .anyMatch(i -> i.getProduct().getId().equals(productId)))
                .orElse(false);
        }

        Review review = Review.builder()
            .product(product).user(user)
            .rating(req.getRating()).title(req.getTitle()).body(req.getBody())
            .verifiedPurchase(verified).approved(true)
            .build();

        Review saved = reviewRepository.save(review);
        updateProductRating(productId);
        return toDto(saved);
    }

    @Transactional
    public void deleteReview(Long reviewId, String email) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        if (!review.getUser().getEmail().equals(email))
            throw new ForbiddenException("Cannot delete another user's review");
        reviewRepository.delete(review);
        updateProductRating(review.getProduct().getId());
    }

    private void updateProductRating(Long productId) {
        Double avg   = reviewRepository.getAverageRating(productId);
        Long   count = reviewRepository.getReviewCount(productId);
        productRepository.findById(productId).ifPresent(p -> {
            p.setAverageRating(avg != null
                ? BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);
            p.setTotalReviews(count != null ? count.intValue() : 0);
            productRepository.save(p);
        });
    }

    private ReviewDto toDto(Review r) {
        return ReviewDto.builder()
            .id(r.getId()).productId(r.getProduct().getId())
            .userId(r.getUser().getId())
            .userName(r.getUser().getFirstName() + " " + r.getUser().getLastName())
            .rating(r.getRating()).title(r.getTitle()).body(r.getBody())
            .isVerifiedPurchase(r.isVerifiedPurchase())
            .helpfulCount(r.getHelpfulCount()).createdAt(r.getCreatedAt())
            .build();
    }
}
