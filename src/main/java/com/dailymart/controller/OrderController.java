package com.dailymart.controller;

import com.dailymart.dto.*;
import com.dailymart.entity.User;
import com.dailymart.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order placement, tracking, and management")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/place")
    @Operation(summary = "Place a new order from cart")
    public ResponseEntity<OrderDto> placeOrder(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody PlaceOrderRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(orderService.placeOrder(user.getId(), req));
    }

    @GetMapping
    @Operation(summary = "Get current user orders with pagination")
    public ResponseEntity<Page<OrderDto>> getMyOrders(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(orderService.getUserOrders(user.getId(), page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order details by order ID")
    public ResponseEntity<OrderDto> getOrderById(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        boolean isAdmin = user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(orderService.getOrderById(id, user.getId(), isAdmin));
    }

    @GetMapping("/by-number/{orderNumber}")
    @Operation(summary = "Get order details by order number")
    public ResponseEntity<OrderDto> getOrderByNumber(
            @AuthenticationPrincipal User user,
            @PathVariable String orderNumber) {
        boolean isAdmin = user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(orderService.getOrderByOrderNumber(orderNumber, user.getId(), isAdmin));
    }

    @GetMapping("/{id}/tracking")
    @Operation(summary = "Get real-time order delivery tracking timeline")
    public ResponseEntity<List<TrackingDto>> getOrderTracking(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        boolean isAdmin = user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(orderService.getOrderTracking(id, user.getId(), isAdmin));
    }

    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel an eligible order")
    public ResponseEntity<OrderDto> cancelOrder(
            @AuthenticationPrincipal User user,
            @PathVariable Long orderId,
            @RequestParam String reason) {
        return ResponseEntity.ok(orderService.cancelOrder(orderId, user.getId(), reason));
    }
}
