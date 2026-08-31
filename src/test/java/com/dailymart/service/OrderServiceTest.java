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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private CartRepository cartRepository;
    @Mock private ProductRepository productRepository;
    @Mock private AddressRepository addressRepository;
    @Mock private UserRepository userRepository;
    @Mock private OrderTrackingRepository trackingRepository;
    @Mock private CouponService couponService;
    @Mock private EmailService emailService;

    @InjectMocks
    private OrderService orderService;

    private User testUser;
    private Address testAddress;
    private Product testProduct;
    private Cart testCart;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).email("user@example.com").build();
        testAddress = Address.builder()
                .id(1L)
                .user(testUser)
                .fullName("John Doe")
                .phone("9876543210")
                .addressLine1("123 Street")
                .city("Bangalore")
                .state("Karnataka")
                .pincode("560001")
                .build();

        testProduct = Product.builder()
                .id(1L)
                .name("Basmati Rice")
                .sellingPrice(new BigDecimal("500.00"))
                .stockQuantity(20)
                .images(new ArrayList<>())
                .build();

        CartItem item = CartItem.builder()
                .id(1L)
                .product(testProduct)
                .quantity(2)
                .unitPrice(new BigDecimal("500.00"))
                .build();

        testCart = Cart.builder()
                .id(1L)
                .user(testUser)
                .items(new ArrayList<>(List.of(item)))
                .build();

        testOrder = Order.builder()
                .id(1L)
                .orderNumber("DM123456789")
                .user(testUser)
                .address(testAddress)
                .subtotal(new BigDecimal("1000.00"))
                .deliveryCharges(BigDecimal.ZERO)
                .taxAmount(new BigDecimal("180.00"))
                .totalAmount(new BigDecimal("1180.00"))
                .orderStatus(Order.OrderStatus.PLACED)
                .paymentStatus(Order.PaymentStatus.PENDING)
                .paymentMethod(Order.PaymentMethod.COD)
                .orderItems(new ArrayList<>())
                .trackingHistory(new ArrayList<>())
                .build();
    }

    @Test
    void placeOrder_Success() {
        PlaceOrderRequest req = new PlaceOrderRequest(1L, "COD", null, null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(addressRepository.findById(1L)).thenReturn(Optional.of(testAddress));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> {
            Order o = i.getArgument(0);
            o.setId(1L);
            return o;
        });

        OrderDto orderDto = orderService.placeOrder(1L, req);

        assertNotNull(orderDto);
        assertNotNull(orderDto.getOrderNumber());
        assertEquals(new BigDecimal("1000.00"), orderDto.getSubtotal());
        assertEquals("PLACED", orderDto.getOrderStatus());
        assertTrue(testCart.getItems().isEmpty());
        verify(productRepository, times(1)).decrementStock(1L, 2);
        verify(trackingRepository, times(1)).save(any(OrderTracking.class));
    }

    @Test
    void placeOrder_EmptyCart_ThrowsBadRequest() {
        testCart.getItems().clear();
        PlaceOrderRequest req = new PlaceOrderRequest(1L, "COD", null, null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));

        assertThrows(BadRequestException.class, () -> orderService.placeOrder(1L, req));
    }

    @Test
    void cancelOrder_PlacedOrder_Success() {
        OrderItem orderItem = OrderItem.builder()
                .id(1L)
                .order(testOrder)
                .product(testProduct)
                .quantity(2)
                .build();
        testOrder.setOrderItems(List.of(orderItem));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        OrderDto result = orderService.cancelOrder(1L, 1L, "Changed my mind");

        assertEquals("CANCELLED", result.getOrderStatus());
        assertEquals(22, testProduct.getStockQuantity());
        verify(trackingRepository, times(1)).save(any(OrderTracking.class));
    }
}
