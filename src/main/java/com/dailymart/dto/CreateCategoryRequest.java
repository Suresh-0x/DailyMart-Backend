package com.dailymart.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class CreateCategoryRequest {
    @NotBlank String name;
    String description;
    String imageUrl;
    Long parentId;
    int displayOrder;
}
