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
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.ArrayList;
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
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id)));
    }

    public ProductDto getProductBySlug(String slug) {
        return toDto(productRepository.findBySlug(slug)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with slug: " + slug)));
    }

    public Page<ProductDto> getFeaturedProducts(int page, int size) {
        return productRepository.findByFeaturedTrueAndActiveTrue(PageRequest.of(page, size)).map(this::toDto);
    }

    @Transactional
    public ProductDto createProduct(CreateProductRequest req) {
        Category category = categoryRepository.findById(req.getCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + req.getCategoryId()));

        BigDecimal discount = calculateDiscount(req.getOriginalPrice(), req.getSellingPrice());

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
            .images(new ArrayList<>())
            .reviews(new ArrayList<>())
            .build();

        if (req.getImageUrls() != null && !req.getImageUrls().isEmpty()) {
            List<ProductImage> images = new ArrayList<>();
            for (int i = 0; i < req.getImageUrls().size(); i++) {
                images.add(ProductImage.builder()
                    .product(product)
                    .imageUrl(req.getImageUrls().get(i))
                    .altText(req.getName())
                    .primary(i == 0)
                    .displayOrder(i)
                    .build());
            }
            product.setImages(images);
        }

        return toDto(productRepository.save(product));
    }

    @Transactional
    public ProductDto updateProduct(Long id, CreateProductRequest req) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        if (req.getCategoryId() != null) {
            Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + req.getCategoryId()));
            product.setCategory(category);
        }
        if (req.getName() != null)             product.setName(req.getName());
        if (req.getDescription() != null)      product.setDescription(req.getDescription());
        if (req.getShortDescription() != null) product.setShortDescription(req.getShortDescription());
        if (req.getSku() != null)              product.setSku(req.getSku());
        if (req.getBrand() != null)            product.setBrand(req.getBrand());
        if (req.getOriginalPrice() != null)    product.setOriginalPrice(req.getOriginalPrice());
        if (req.getSellingPrice() != null)     product.setSellingPrice(req.getSellingPrice());
        if (req.getStockQuantity() != null)    product.setStockQuantity(req.getStockQuantity());
        product.setFeatured(req.isFeatured());

        if (product.getOriginalPrice() != null && product.getSellingPrice() != null) {
            product.setDiscountPercent(calculateDiscount(product.getOriginalPrice(), product.getSellingPrice()));
        }

        if (req.getImageUrls() != null) {
            product.getImages().clear();
            for (int i = 0; i < req.getImageUrls().size(); i++) {
                product.getImages().add(ProductImage.builder()
                    .product(product)
                    .imageUrl(req.getImageUrls().get(i))
                    .altText(product.getName())
                    .primary(i == 0)
                    .displayOrder(i)
                    .build());
            }
        }

        return toDto(productRepository.save(product));
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        product.setActive(false);
        productRepository.save(product);
    }

    private BigDecimal calculateDiscount(BigDecimal originalPrice, BigDecimal sellingPrice) {
        if (originalPrice != null && sellingPrice != null
                && originalPrice.compareTo(BigDecimal.ZERO) > 0
                && originalPrice.compareTo(sellingPrice) > 0) {
            return originalPrice.subtract(sellingPrice)
                .divide(originalPrice, 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        }
        return BigDecimal.ZERO;
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

    public ProductDto toDto(Product p) {
        return ProductDto.builder()
            .id(p.getId())
            .name(p.getName())
            .slug(p.getSlug())
            .shortDescription(p.getShortDescription())
            .description(p.getDescription())
            .sku(p.getSku())
            .brand(p.getBrand())
            .originalPrice(p.getOriginalPrice())
            .sellingPrice(p.getSellingPrice())
            .discountPercent(p.getDiscountPercent())
            .stockQuantity(p.getStockQuantity())
            .averageRating(p.getAverageRating())
            .totalReviews(p.getTotalReviews())
            .totalSold(p.getTotalSold())
            .isActive(p.isActive())
            .isFeatured(p.isFeatured())
            .categoryId(p.getCategory() != null ? p.getCategory().getId() : null)
            .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
            .images(p.getImages() != null
                ? p.getImages().stream()
                    .map(img -> new ProductImageDto(img.getId(), img.getImageUrl(), img.isPrimary()))
                    .toList()
                : List.of())
            .createdAt(p.getCreatedAt())
            .build();
    }
}
