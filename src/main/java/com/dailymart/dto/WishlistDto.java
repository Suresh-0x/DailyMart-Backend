package com.dailymart.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class WishlistDto {
    private Long id;
    private ProductDto product;
    private LocalDateTime addedAt;
}
