package com.dailymart.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class CreateReviewRequest {
    @NotNull @Min(1) @Max(5)
    private Integer rating;
    @Size(max = 200)
    private String title;
    @Size(max = 2000)
    private String body;
    private Long orderId;
}
