package com.dailymart.service;

import com.dailymart.dto.*;
import com.dailymart.entity.*;
import com.dailymart.exception.*;
import com.dailymart.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final OrderTrackingRepository trackingRepository;
    private final CouponService couponService;
    private final EmailService emailService;

    @Transactional
    public OrderDto placeOrder(Long userId, PlaceOrderRequest req) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Cart cart = cartRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty. Add products before checkout.");
        }

        Address address = addressRepository.findById(req.getAddressId())
            .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + req.getAddressId()));
        if (!address.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Address does not belong to the user");
        }

        BigDecimal subtotal = cart.getItems().stream()
            .map(CartItem::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Coupon discount
        BigDecimal discount = BigDecimal.ZERO;
        if (req.getCouponCode() != null && !req.getCouponCode().trim().isEmpty()) {
            try {
                CouponDto couponResult = couponService.validateAndApplyCoupon(req.getCouponCode(), subtotal);
                discount = couponResult.getDiscountAmount();
                couponService.incrementUsage(req.getCouponCode());
            } catch (Exception e) {
                log.warn("Coupon application skipped for {}: {}", req.getCouponCode(), e.getMessage());
            }
        }

        BigDecimal discountedSubtotal = subtotal.subtract(discount).max(BigDecimal.ZERO);
        BigDecimal delivery = discountedSubtotal.compareTo(new BigDecimal("499")) >= 0
            ? BigDecimal.ZERO : new BigDecimal("49");
        BigDecimal tax = discountedSubtotal.multiply(new BigDecimal("0.18"))
            .setScale(2, java.math.RoundingMode.HALF_UP);
        BigDecimal total = discountedSubtotal.add(delivery).add(tax);

        Order.PaymentMethod paymentMethod;
        try {
            paymentMethod = Order.PaymentMethod.valueOf(req.getPaymentMethod().toUpperCase());
        } catch (Exception e) {
            throw new BadRequestException("Invalid payment method: " + req.getPaymentMethod());
        }

        Order order = Order.builder()
            .orderNumber("DM" + System.currentTimeMillis())
            .user(user)
            .address(address)
            .subtotal(subtotal)
            .discountAmount(discount)
            .deliveryCharges(delivery)
            .taxAmount(tax)
            .totalAmount(total)
            .paymentMethod(paymentMethod)
            .orderStatus(Order.OrderStatus.PLACED)
            .paymentStatus(paymentMethod == Order.PaymentMethod.COD ? Order.PaymentStatus.PENDING : Order.PaymentStatus.PENDING)
            .estimatedDelivery(LocalDate.now().plusDays(4))
            .orderItems(new ArrayList<>())
            .trackingHistory(new ArrayList<>())
            .build();

        List<OrderItem> items = new ArrayList<>();
        for (CartItem ci : cart.getItems()) {
            Product prod = productRepository.findById(ci.getProduct().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + ci.getProduct().getId()));

            if (prod.getStockQuantity() < ci.getQuantity()) {
                throw new BadRequestException("Insufficient stock for product: " + prod.getName() +
                        " (Available: " + prod.getStockQuantity() + ")");
            }

            String primaryImage = null;
            if (prod.getImages() != null && !prod.getImages().isEmpty()) {
                primaryImage = prod.getImages().stream()
                        .filter(ProductImage::isPrimary)
                        .findFirst()
                        .map(ProductImage::getImageUrl)
                        .orElse(prod.getImages().get(0).getImageUrl());
            }

            items.add(OrderItem.builder()
                .order(order)
                .product(prod)
                .productName(prod.getName())
                .productImage(primaryImage)
                .quantity(ci.getQuantity())
                .unitPrice(ci.getUnitPrice())
                .totalPrice(ci.getTotalPrice())
                .build());

            productRepository.decrementStock(prod.getId(), ci.getQuantity());
        }
        order.setOrderItems(items);
        Order saved = orderRepository.save(order);

        OrderTracking initialTracking = OrderTracking.builder()
            .order(saved)
            .status(Order.OrderStatus.PLACED)
            .description("Your order has been placed successfully and is being prepared.")
            .location("DailyMart Fulfillment Hub")
            .build();
        trackingRepository.save(initialTracking);

        cart.getItems().clear();
        cartRepository.save(cart);

        // Async Email confirmation
        try {
            emailService.sendOrderConfirmation(user.getEmail(), saved.getOrderNumber(), "₹" + saved.getTotalAmount());
        } catch (Exception e) {
            log.error("Failed to send order email: {}", e.getMessage());
        }

        return toDto(saved);
    }

    public Page<OrderDto> getUserOrders(Long userId, int page, int size) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size))
            .map(this::toDto);
    }

    public OrderDto getOrderById(Long orderId, Long userId, boolean isAdmin) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        if (!isAdmin && !order.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You are not authorized to view this order");
        }
        return toDto(order);
    }

    public OrderDto getOrderByOrderNumber(String orderNumber, Long userId, boolean isAdmin) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with order number: " + orderNumber));
        if (!isAdmin && !order.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You are not authorized to view this order");
        }
        return toDto(order);
    }

    public List<TrackingDto> getOrderTracking(Long orderId, Long userId, boolean isAdmin) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        if (!isAdmin && !order.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You are not authorized to view tracking for this order");
        }
        return trackingRepository.findByOrderIdOrderByCreatedAtAsc(orderId).stream()
            .map(t -> TrackingDto.builder()
                .status(t.getStatus() != null ? t.getStatus().name() : null)
                .description(t.getDescription())
                .location(t.getLocation())
                .timestamp(t.getCreatedAt())
                .build())
            .toList();
    }

    @Transactional
    public OrderDto cancelOrder(Long orderId, Long userId, String reason) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        if (!order.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Unauthorized to cancel this order");
        }
        if (!List.of(Order.OrderStatus.PLACED, Order.OrderStatus.CONFIRMED).contains(order.getOrderStatus())) {
            throw new BadRequestException("Cannot cancel order at status: " + order.getOrderStatus());
        }

        order.setOrderStatus(Order.OrderStatus.CANCELLED);
        order.setCancellationReason(reason);

        // Restore stock
        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                Product p = item.getProduct();
                p.setStockQuantity(p.getStockQuantity() + item.getQuantity());
                productRepository.save(p);
            }
        }

        Order saved = orderRepository.save(order);

        trackingRepository.save(OrderTracking.builder()
            .order(saved)
            .status(Order.OrderStatus.CANCELLED)
            .description("Order cancelled by customer. Reason: " + reason)
            .location("DailyMart Operations")
            .build());

        return toDto(saved);
    }

    public OrderDto toDto(Order o) {
        AddressDto addressDto = null;
        if (o.getAddress() != null) {
            Address a = o.getAddress();
            addressDto = AddressDto.builder()
                .id(a.getId())
                .addressType(a.getAddressType() != null ? a.getAddressType().name() : null)
                .fullName(a.getFullName())
                .phone(a.getPhone())
                .addressLine1(a.getAddressLine1())
                .addressLine2(a.getAddressLine2())
                .city(a.getCity())
                .state(a.getState())
                .pincode(a.getPincode())
                .country(a.getCountry())
                .isDefault(a.isDefaultAddress())
                .build();
        }

        List<OrderItemDto> itemDtos = o.getOrderItems() != null ? o.getOrderItems().stream().map(i ->
            OrderItemDto.builder()
                .id(i.getId())
                .productId(i.getProduct() != null ? i.getProduct().getId() : null)
                .productName(i.getProductName())
                .productImage(i.getProductImage())
                .quantity(i.getQuantity())
                .unitPrice(i.getUnitPrice())
                .totalPrice(i.getTotalPrice())
                .build()).toList()
            : List.of();

        List<TrackingDto> trackingDtos = o.getTrackingHistory() != null ? o.getTrackingHistory().stream().map(t ->
            TrackingDto.builder()
                .status(t.getStatus() != null ? t.getStatus().name() : null)
                .description(t.getDescription())
                .location(t.getLocation())
                .timestamp(t.getCreatedAt())
                .build()).toList()
            : List.of();

        return OrderDto.builder()
            .id(o.getId())
            .orderNumber(o.getOrderNumber())
            .subtotal(o.getSubtotal())
            .discountAmount(o.getDiscountAmount())
            .deliveryCharges(o.getDeliveryCharges())
            .taxAmount(o.getTaxAmount())
            .totalAmount(o.getTotalAmount())
            .orderStatus(o.getOrderStatus() != null ? o.getOrderStatus().name() : null)
            .paymentStatus(o.getPaymentStatus() != null ? o.getPaymentStatus().name() : null)
            .paymentMethod(o.getPaymentMethod() != null ? o.getPaymentMethod().name() : null)
            .estimatedDelivery(o.getEstimatedDelivery())
            .createdAt(o.getCreatedAt())
            .deliveryAddress(addressDto)
            .items(itemDtos)
            .tracking(trackingDtos)
            .build();
    }
}
