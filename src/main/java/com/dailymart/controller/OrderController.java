package com.dailymart.controller;

import com.dailymart.dto.*;
import com.dailymart.entity.User;
import com.dailymart.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/place")
    public ResponseEntity<OrderDto> placeOrder(
            @AuthenticationPrincipal User user,
            @RequestBody PlaceOrderRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(orderService.placeOrder(user.getId(), req));
    }

    @GetMapping
    public ResponseEntity<Page<OrderDto>> getMyOrders(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(orderService.getUserOrders(user.getId(), page, size));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderDto> cancelOrder(
            @AuthenticationPrincipal User user,
            @PathVariable Long orderId,
            @RequestParam String reason) {
        return ResponseEntity.ok(orderService.cancelOrder(orderId, user.getId(), reason));
    }
}
