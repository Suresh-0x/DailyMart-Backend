package com.dailymart.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderDto {
    private Long id;
    private String orderNumber;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal deliveryCharges;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private String paymentStatus;
    private String orderStatus;
    private LocalDate estimatedDelivery;
    private LocalDateTime createdAt;
    private AddressDto deliveryAddress;
    private List<OrderItemDto> items;
    private List<TrackingDto> tracking;
}
