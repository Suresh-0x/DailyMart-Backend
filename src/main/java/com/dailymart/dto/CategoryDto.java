package com.dailymart.dto;

import lombok.*;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CategoryDto {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String imageUrl;
    private Long parentId;
    private String parentName;
    private boolean isActive;
    private int displayOrder;
    private int productCount;
    private List<CategoryDto> children;
}
