package com.dailymart.controller;

import com.dailymart.dto.*;
import com.dailymart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartDto> getCart(@AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(cartService.getCart(ud.getUsername()));
    }

    @PostMapping("/add")
    public ResponseEntity<CartDto> addToCart(
            @AuthenticationPrincipal UserDetails ud,
            @RequestBody AddToCartRequest req) {
        return ResponseEntity.ok(cartService.addToCart(ud.getUsername(), req));
    }

    @PutMapping("/update/{itemId}")
    public ResponseEntity<CartDto> updateQuantity(
            @AuthenticationPrincipal UserDetails ud,
            @PathVariable Long itemId,
            @RequestParam int quantity) {
        return ResponseEntity.ok(cartService.updateQuantity(ud.getUsername(), itemId, quantity));
    }

    @DeleteMapping("/remove/{itemId}")
    public ResponseEntity<CartDto> removeItem(
            @AuthenticationPrincipal UserDetails ud,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.removeItem(ud.getUsername(), itemId));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal UserDetails ud) {
        cartService.clearCart(ud.getUsername());
        return ResponseEntity.noContent().build();
    }
}
