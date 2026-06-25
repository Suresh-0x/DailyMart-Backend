package com.dailymart.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductDto {
    private Long id;
    private String name;
    private String slug;
    private String shortDescription;
    private String description;
    private String sku;
    private String brand;
    private Long categoryId;
    private String categoryName;
    private BigDecimal originalPrice;
    private BigDecimal sellingPrice;
    private BigDecimal discountPercent;
    private int stockQuantity;
    private boolean isActive;
    private boolean isFeatured;
    private BigDecimal averageRating;
    private int totalReviews;
    private int totalSold;
    private List<ProductImageDto> images;
    private LocalDateTime createdAt;
}
