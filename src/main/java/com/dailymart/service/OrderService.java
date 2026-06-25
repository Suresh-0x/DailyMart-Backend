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
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;
    private final OrderTrackingRepository trackingRepository;

    @Transactional
    public OrderDto placeOrder(Long userId, PlaceOrderRequest req) {
        Cart cart = cartRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        if (cart.getItems().isEmpty()) throw new BadRequestException("Cart is empty");

        Address address = addressRepository.findById(req.getAddressId())
            .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        BigDecimal subtotal = cart.getItems().stream()
            .map(CartItem::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal delivery = subtotal.compareTo(new BigDecimal("499")) >= 0
            ? BigDecimal.ZERO : new BigDecimal("49");
        BigDecimal tax = subtotal.multiply(new BigDecimal("0.18"))
            .setScale(2, java.math.RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(delivery).add(tax);

        Order order = Order.builder()
            .orderNumber("DM" + System.currentTimeMillis())
            .user(User.builder().id(userId).build())
            .address(address).subtotal(subtotal)
            .deliveryCharges(delivery).taxAmount(tax).totalAmount(total)
            .paymentMethod(Order.PaymentMethod.valueOf(req.getPaymentMethod()))
            .orderStatus(Order.OrderStatus.PLACED)
            .paymentStatus(Order.PaymentStatus.PENDING)
            .estimatedDelivery(LocalDate.now().plusDays(5))
            .build();

        List<OrderItem> items = new ArrayList<>();
        for (CartItem ci : cart.getItems()) {
            if (ci.getProduct().getStockQuantity() < ci.getQuantity())
                throw new BadRequestException("Insufficient stock for: " + ci.getProduct().getName());
            items.add(OrderItem.builder()
                .order(order).product(ci.getProduct())
                .productName(ci.getProduct().getName())
                .quantity(ci.getQuantity()).unitPrice(ci.getUnitPrice())
                .totalPrice(ci.getTotalPrice()).build());
            productRepository.decrementStock(ci.getProduct().getId(), ci.getQuantity());
        }
        order.setOrderItems(items);
        Order saved = orderRepository.save(order);

        trackingRepository.save(OrderTracking.builder()
            .order(saved).status(Order.OrderStatus.PLACED)
            .description("Your order has been placed successfully").build());

        cart.getItems().clear();
        cartRepository.save(cart);
        return toDto(saved);
    }

    public Page<OrderDto> getUserOrders(Long userId, int page, int size) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size))
            .map(this::toDto);
    }

    @Transactional
    public OrderDto cancelOrder(Long orderId, Long userId, String reason) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (!order.getUser().getId().equals(userId)) throw new ForbiddenException("Unauthorized");
        if (!List.of(Order.OrderStatus.PLACED, Order.OrderStatus.CONFIRMED)
                .contains(order.getOrderStatus()))
            throw new BadRequestException("Cannot cancel order at this stage");

        order.setOrderStatus(Order.OrderStatus.CANCELLED);
        order.setCancellationReason(reason);
        Order saved = orderRepository.save(order);

        trackingRepository.save(OrderTracking.builder()
            .order(saved).status(Order.OrderStatus.CANCELLED)
            .description("Order cancelled: " + reason).build());
        return toDto(saved);
    }

    private OrderDto toDto(Order o) {
        return OrderDto.builder()
            .id(o.getId()).orderNumber(o.getOrderNumber())
            .totalAmount(o.getTotalAmount()).orderStatus(o.getOrderStatus().name())
            .paymentStatus(o.getPaymentStatus().name()).paymentMethod(o.getPaymentMethod().name())
            .estimatedDelivery(o.getEstimatedDelivery()).createdAt(o.getCreatedAt())
            .items(o.getOrderItems() != null ? o.getOrderItems().stream().map(i ->
                OrderItemDto.builder().productId(i.getProduct().getId())
                    .productName(i.getProductName()).quantity(i.getQuantity())
                    .unitPrice(i.getUnitPrice()).totalPrice(i.getTotalPrice()).build()).toList()
                : List.of())
            .build();
    }
}
