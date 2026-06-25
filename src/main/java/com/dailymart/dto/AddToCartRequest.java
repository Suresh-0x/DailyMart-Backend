package com.dailymart.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class AddToCartRequest {
    @NotNull Long productId;
    @Min(1) @Max(10) int quantity = 1;
}
