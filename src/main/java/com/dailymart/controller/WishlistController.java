package com.dailymart.controller;

import com.dailymart.dto.*;
import com.dailymart.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public ResponseEntity<List<WishlistDto>> getWishlist(@AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(wishlistService.getWishlist(ud.getUsername()));
    }

    @PostMapping("/toggle/{productId}")
    public ResponseEntity<MessageResponse> toggleWishlist(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(wishlistService.toggleWishlist(productId, ud.getUsername()));
    }
}
