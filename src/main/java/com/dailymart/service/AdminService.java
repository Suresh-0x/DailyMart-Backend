package com.dailymart.service;

import com.dailymart.dto.*;
import com.dailymart.entity.*;
import com.dailymart.exception.*;
import com.dailymart.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderTrackingRepository trackingRepository;

    public DashboardDto getDashboardStats() {
        BigDecimal revenue = orderRepository.sumRevenue();
        return DashboardDto.builder()
            .totalUsers(userRepository.count())
            .totalProducts(productRepository.count())
            .totalOrders(orderRepository.countAllOrders())
            .totalRevenue(revenue != null ? revenue : BigDecimal.ZERO)
            .build();
    }

    public Page<UserDto> getAllUsers(int page, int size) {
        return userRepository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()))
            .map(u -> UserDto.builder()
                .id(u.getId()).firstName(u.getFirstName()).lastName(u.getLastName())
                .email(u.getEmail()).phone(u.getPhone()).enabled(u.isEnabled())
                .roles(u.getRoles().stream().map(r -> r.getName().name()).toList())
                .createdAt(u.getCreatedAt()).build());
    }

    @Transactional
    public MessageResponse toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setAccountNonLocked(!user.isAccountNonLocked());
        userRepository.save(user);
        return new MessageResponse(user.isAccountNonLocked() ? "User activated" : "User blocked");
    }

    public Page<OrderDto> getAllOrders(int page, int size) {
        return orderRepository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()))
            .map(this::toOrderDto);
    }

    @Transactional
    public OrderDto updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        Order.OrderStatus newStatus = Order.OrderStatus.valueOf(status);
        order.setOrderStatus(newStatus);
        Order saved = orderRepository.save(order);
        trackingRepository.save(OrderTracking.builder()
            .order(saved).status(newStatus)
            .description("Order status updated to " + status).build());
        return toOrderDto(saved);
    }

    private OrderDto toOrderDto(Order o) {
        return OrderDto.builder()
            .id(o.getId()).orderNumber(o.getOrderNumber())
            .totalAmount(o.getTotalAmount()).orderStatus(o.getOrderStatus().name())
            .paymentStatus(o.getPaymentStatus().name()).paymentMethod(o.getPaymentMethod().name())
            .createdAt(o.getCreatedAt()).build();
    }
}
