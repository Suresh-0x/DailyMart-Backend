package com.dailymart.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class PlaceOrderRequest {
    @NotNull Long addressId;
    @NotBlank String paymentMethod;
    String couponCode;
    String notes;
}
