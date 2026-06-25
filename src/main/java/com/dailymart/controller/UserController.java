package com.dailymart.controller;

import com.dailymart.dto.*;
import com.dailymart.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile and address management")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserDto> getProfile(@AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(userService.getProfile(ud.getUsername()));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserDto> updateProfile(
            @AuthenticationPrincipal UserDetails ud,
            @RequestBody UpdateProfileRequest req) {
        return ResponseEntity.ok(userService.updateProfile(ud.getUsername(), req));
    }

    @PostMapping("/change-password")
    public ResponseEntity<MessageResponse> changePassword(
            @AuthenticationPrincipal UserDetails ud,
            @Valid @RequestBody ChangePasswordRequest req) {
        return ResponseEntity.ok(userService.changePassword(ud.getUsername(), req));
    }

    @GetMapping("/addresses")
    public ResponseEntity<List<AddressDto>> getAddresses(@AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(userService.getAddresses(ud.getUsername()));
    }

    @PostMapping("/addresses")
    public ResponseEntity<AddressDto> addAddress(
            @AuthenticationPrincipal UserDetails ud,
            @Valid @RequestBody CreateAddressRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(userService.addAddress(ud.getUsername(), req));
    }

    @DeleteMapping("/addresses/{id}")
    public ResponseEntity<Void> deleteAddress(
            @AuthenticationPrincipal UserDetails ud,
            @PathVariable Long id) {
        userService.deleteAddress(ud.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}
