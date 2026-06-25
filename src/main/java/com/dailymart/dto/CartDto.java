package com.dailymart.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CartDto {
    private Long cartId;
    private List<CartItemDto> items;
    private int totalItems;
    private BigDecimal subtotal;
    private BigDecimal deliveryCharges;
    private BigDecimal total;
    private BigDecimal savings;
}
