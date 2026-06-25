package com.dailymart.service;

import com.dailymart.dto.*;
import com.dailymart.entity.*;
import com.dailymart.exception.ResourceNotFoundException;
import com.dailymart.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public Page<ProductDto> getAllProducts(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
            ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        return productRepository.findByActiveTrue(PageRequest.of(page, size, sort)).map(this::toDto);
    }

    public Page<ProductDto> getProductsByCategory(Long categoryId, int page, int size) {
        return productRepository.findByCategoryIdAndActiveTrue(
            categoryId, PageRequest.of(page, size, Sort.by("createdAt").descending())).map(this::toDto);
    }

    public Page<ProductDto> searchProducts(String query, int page, int size) {
        return productRepository.searchProducts(query, PageRequest.of(page, size)).map(this::toDto);
    }

    public ProductDto getProductById(Long id) {
        return toDto(productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found")));
    }

    public Page<ProductDto> getFeaturedProducts(int page, int size) {
        return productRepository.findByFeaturedTrueAndActiveTrue(PageRequest.of(page, size)).map(this::toDto);
    }

    @Transactional
    public ProductDto createProduct(CreateProductRequest req) {
        Category category = categoryRepository.findById(req.getCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        BigDecimal discount = BigDecimal.ZERO;
        if (req.getOriginalPrice().compareTo(BigDecimal.ZERO) > 0
                && req.getSellingPrice().compareTo(BigDecimal.ZERO) > 0) {
            discount = req.getOriginalPrice().subtract(req.getSellingPrice())
                .divide(req.getOriginalPrice(), 2, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        }

        Product product = Product.builder()
            .name(req.getName())
            .slug(generateSlug(req.getName()))
            .description(req.getDescription())
            .shortDescription(req.getShortDescription())
            .sku(req.getSku())
            .category(category)
            .brand(req.getBrand())
            .originalPrice(req.getOriginalPrice())
            .sellingPrice(req.getSellingPrice())
            .discountPercent(discount)
            .stockQuantity(req.getStockQuantity() != null ? req.getStockQuantity() : 0)
            .featured(req.isFeatured())
            .active(true)
            .build();

        return toDto(productRepository.save(product));
    }

    @Transactional
    public ProductDto updateProduct(Long id, CreateProductRequest req) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        if (req.getCategoryId() != null) {
            Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            product.setCategory(category);
        }
        if (req.getName() != null)          product.setName(req.getName());
        if (req.getDescription() != null)   product.setDescription(req.getDescription());
        if (req.getOriginalPrice() != null) product.setOriginalPrice(req.getOriginalPrice());
        if (req.getSellingPrice() != null)  product.setSellingPrice(req.getSellingPrice());
        if (req.getStockQuantity() != null) product.setStockQuantity(req.getStockQuantity());
        return toDto(productRepository.save(product));
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        product.setActive(false);
        productRepository.save(product);
    }

    private String generateSlug(String name) {
        String normalized = Normalizer.normalize(name, Normalizer.Form.NFD);
        return Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
            .matcher(normalized).replaceAll("")
            .toLowerCase()
            .replaceAll("[^a-z0-9\\s-]", "")
            .replaceAll("\\s+", "-")
            .replaceAll("-+", "-")
            + "-" + System.currentTimeMillis();
    }

    private ProductDto toDto(Product p) {
        return ProductDto.builder()
            .id(p.getId())
            .name(p.getName())
            .slug(p.getSlug())
            .shortDescription(p.getShortDescription())
            .description(p.getDescription())
            .brand(p.getBrand())
            .originalPrice(p.getOriginalPrice())
            .sellingPrice(p.getSellingPrice())
            .discountPercent(p.getDiscountPercent())
            .stockQuantity(p.getStockQuantity())
            .averageRating(p.getAverageRating())
            .totalReviews(p.getTotalReviews())
            .isFeatured(p.isFeatured())
            .categoryId(p.getCategory().getId())
            .categoryName(p.getCategory().getName())
            .images(p.getImages() != null
                ? p.getImages().stream()
                    .map(img -> new ProductImageDto(img.getId(), img.getImageUrl(), img.isPrimary()))
                    .toList()
                : List.of())
            .build();
    }
}
