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
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderTrackingRepository trackingRepository;
    private final ProductService productService;
    private final OrderService orderService;

    public DashboardDto getDashboardStats() {
        BigDecimal revenue = orderRepository.sumRevenue();
        long pendingOrders = orderRepository.countByOrderStatus(Order.OrderStatus.PLACED)
                + orderRepository.countByOrderStatus(Order.OrderStatus.CONFIRMED)
                + orderRepository.countByOrderStatus(Order.OrderStatus.PROCESSING);
        long deliveredOrders = orderRepository.countByOrderStatus(Order.OrderStatus.DELIVERED);

        Map<String, Long> ordersByStatus = new HashMap<>();
        for (Order.OrderStatus status : Order.OrderStatus.values()) {
            ordersByStatus.put(status.name(), orderRepository.countByOrderStatus(status));
        }

        List<OrderDto> recentOrders = orderRepository.findTop5ByOrderByCreatedAtDesc().stream()
                .map(orderService::toDto)
                .toList();

        List<ProductDto> topProducts = productRepository.findTop5ByActiveTrueOrderByTotalSoldDesc().stream()
                .map(productService::toDto)
                .toList();

        return DashboardDto.builder()
            .totalUsers(userRepository.count())
            .totalProducts(productRepository.count())
            .totalOrders(orderRepository.countAllOrders())
            .totalRevenue(revenue != null ? revenue : BigDecimal.ZERO)
            .pendingOrders(pendingOrders)
            .deliveredOrders(deliveredOrders)
            .recentOrders(recentOrders)
            .topProducts(topProducts)
            .ordersByStatus(ordersByStatus)
            .revenueByMonth(new HashMap<>())
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
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        user.setAccountNonLocked(!user.isAccountNonLocked());
        userRepository.save(user);
        return new MessageResponse(user.isAccountNonLocked() ? "User unlocked and activated" : "User locked and blocked");
    }

    public Page<OrderDto> getAllOrders(int page, int size) {
        return orderRepository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()))
            .map(orderService::toDto);
    }

    @Transactional
    public OrderDto updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        Order.OrderStatus newStatus;
        try {
            newStatus = Order.OrderStatus.valueOf(status.toUpperCase());
        } catch (Exception e) {
            throw new BadRequestException("Invalid order status: " + status);
        }

        order.setOrderStatus(newStatus);
        if (newStatus == Order.OrderStatus.DELIVERED && order.getPaymentStatus() == Order.PaymentStatus.PENDING) {
            order.setPaymentStatus(Order.PaymentStatus.PAID);
        }

        Order saved = orderRepository.save(order);
        trackingRepository.save(OrderTracking.builder()
            .order(saved)
            .status(newStatus)
            .description("Order status updated to " + newStatus.name())
            .location("DailyMart Delivery Network")
            .build());
        return orderService.toDto(saved);
    }
}
