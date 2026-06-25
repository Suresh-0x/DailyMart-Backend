package com.dailymart.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class RazorpayOrderResponse {
    private String razorpayOrderId;
    private String currency;
    private Long amountInPaise;
    private String keyId;
    private String orderNumber;
}
