package com.dailymart.dto;

import lombok.*;
import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderItemDto {
    private Long id;
    private Long productId;
    private String productName;
    private String productImage;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
}
