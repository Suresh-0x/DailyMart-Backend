package com.dailymart.dto;

import lombok.*;
import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CartItemDto {
    private Long itemId;
    private Long productId;
    private String productName;
    private String productImage;
    private String brand;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal originalPrice;
    private BigDecimal totalPrice;
    private int stockAvailable;
}
