package com.dailymart.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CreateProductRequest {
    @NotBlank(message = "Product name required")
    private String name;
    private String description;
    private String shortDescription;
    private String sku;
    private String brand;
    @NotNull private Long categoryId;
    @NotNull @DecimalMin("0.01") private BigDecimal originalPrice;
    @NotNull @DecimalMin("0.01") private BigDecimal sellingPrice;
    @Min(0) private Integer stockQuantity;
    private boolean isFeatured;
    private List<String> imageUrls;
}
