package com.dailymart.service;

import com.dailymart.dto.*;
import com.dailymart.entity.*;
import com.dailymart.exception.BadRequestException;
import com.dailymart.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock private CartRepository cartRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private CartService cartService;

    private User testUser;
    private Product testProduct;
    private Cart testCart;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).email("user@example.com").build();
        testProduct = Product.builder()
                .id(1L)
                .name("Organic Apples")
                .sellingPrice(new BigDecimal("120.00"))
                .originalPrice(new BigDecimal("150.00"))
                .stockQuantity(25)
                .active(true)
                .build();
        testCart = Cart.builder()
                .id(1L)
                .user(testUser)
                .items(new ArrayList<>())
                .build();
    }

    @Test
    void addToCart_NewItem_Success() {
        AddToCartRequest req = new AddToCartRequest(1L, 2);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        CartDto cartDto = cartService.addToCart("user@example.com", req);

        assertNotNull(cartDto);
        assertEquals(1, testCart.getItems().size());
        assertEquals(2, testCart.getItems().get(0).getQuantity());
        assertEquals(new BigDecimal("240.00"), cartDto.getSubtotal());
    }

    @Test
    void addToCart_ExceedsStock_ThrowsBadRequest() {
        AddToCartRequest req = new AddToCartRequest(1L, 50);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        assertThrows(BadRequestException.class, () -> cartService.addToCart("user@example.com", req));
    }

    @Test
    void updateQuantity_Success() {
        CartItem item = CartItem.builder()
                .id(10L)
                .cart(testCart)
                .product(testProduct)
                .quantity(2)
                .unitPrice(testProduct.getSellingPrice())
                .build();
        testCart.getItems().add(item);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        CartDto updated = cartService.updateQuantity("user@example.com", 10L, 4);

        assertEquals(4, item.getQuantity());
        assertEquals(new BigDecimal("480.00"), updated.getSubtotal());
    }

    @Test
    void removeItem_Success() {
        CartItem item = CartItem.builder()
                .id(10L)
                .cart(testCart)
                .product(testProduct)
                .quantity(2)
                .unitPrice(testProduct.getSellingPrice())
                .build();
        testCart.getItems().add(item);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        CartDto updated = cartService.removeItem("user@example.com", 10L);

        assertTrue(testCart.getItems().isEmpty());
        assertEquals(0, updated.getTotalItems());
    }
}
