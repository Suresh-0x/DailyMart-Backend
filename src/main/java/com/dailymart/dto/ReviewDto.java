package com.dailymart.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ReviewDto {
    private Long id;
    private Long productId;
    private Long userId;
    private String userName;
    private int rating;
    private String title;
    private String body;
    private boolean isVerifiedPurchase;
    private int helpfulCount;
    private LocalDateTime createdAt;
}
