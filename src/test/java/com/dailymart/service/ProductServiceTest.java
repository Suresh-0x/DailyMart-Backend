package com.dailymart.service;

import com.dailymart.dto.*;
import com.dailymart.entity.*;
import com.dailymart.exception.ResourceNotFoundException;
import com.dailymart.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    private Category testCategory;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .id(1L)
                .name("Dairy")
                .slug("dairy")
                .active(true)
                .build();

        testProduct = Product.builder()
                .id(1L)
                .name("Organic Milk")
                .slug("organic-milk")
                .description("Fresh Organic Milk")
                .shortDescription("Milk 1L")
                .sku("MLK-001")
                .brand("Amul")
                .category(testCategory)
                .originalPrice(new BigDecimal("70.00"))
                .sellingPrice(new BigDecimal("60.00"))
                .discountPercent(new BigDecimal("14.29"))
                .stockQuantity(50)
                .featured(true)
                .active(true)
                .images(new ArrayList<>())
                .reviews(new ArrayList<>())
                .build();
    }

    @Test
    void getProductById_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        ProductDto dto = productService.getProductById(1L);

        assertNotNull(dto);
        assertEquals("Organic Milk", dto.getName());
        assertEquals("Amul", dto.getBrand());
        assertEquals(new BigDecimal("60.00"), dto.getSellingPrice());
    }

    @Test
    void getProductById_NotFound_ThrowsException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(99L));
    }

    @Test
    void getProductBySlug_Success() {
        when(productRepository.findBySlug("organic-milk")).thenReturn(Optional.of(testProduct));

        ProductDto dto = productService.getProductBySlug("organic-milk");

        assertNotNull(dto);
        assertEquals("Organic Milk", dto.getName());
    }

    @Test
    void createProduct_Success() {
        CreateProductRequest req = CreateProductRequest.builder()
                .name("Fresh Butter")
                .description("Pure cow butter")
                .shortDescription("Butter 500g")
                .sku("BTR-001")
                .brand("Amul")
                .categoryId(1L)
                .originalPrice(new BigDecimal("250.00"))
                .sellingPrice(new BigDecimal("225.00"))
                .stockQuantity(20)
                .isFeatured(true)
                .imageUrls(List.of("https://example.com/butter.jpg"))
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(2L);
            return p;
        });

        ProductDto created = productService.createProduct(req);

        assertNotNull(created);
        assertEquals("Fresh Butter", created.getName());
        assertEquals(new BigDecimal("10.00"), created.getDiscountPercent());
        assertEquals(1, created.getImages().size());
    }

    @Test
    void searchProducts_Success() {
        Page<Product> productPage = new PageImpl<>(List.of(testProduct));
        when(productRepository.searchProducts(eq("milk"), any(Pageable.class))).thenReturn(productPage);

        Page<ProductDto> result = productService.searchProducts("milk", 0, 10);

        assertEquals(1, result.getTotalElements());
        assertEquals("Organic Milk", result.getContent().get(0).getName());
    }

    @Test
    void deleteProduct_SetsActiveFalse() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        productService.deleteProduct(1L);

        assertFalse(testProduct.isActive());
        verify(productRepository, times(1)).save(testProduct);
    }
}
