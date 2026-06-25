package com.dailymart.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class ProductImageDto {
    private Long id;
    private String imageUrl;
    private boolean isPrimary;
}
