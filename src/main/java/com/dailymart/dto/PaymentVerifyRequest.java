package com.dailymart.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class PaymentVerifyRequest {
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;
    private String orderNumber;
}
