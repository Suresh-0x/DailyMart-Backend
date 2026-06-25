package com.dailymart.service;

import com.dailymart.dto.*;
import com.dailymart.entity.*;
import com.dailymart.exception.*;
import com.dailymart.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartDto getCart(String email) {
        return toDto(getOrCreateCart(getUser(email)));
    }

    @Transactional
    public CartDto addToCart(String email, AddToCartRequest req) {
        Cart cart = getOrCreateCart(getUser(email));
        Product product = productRepository.findById(req.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (!product.isActive())
            throw new BadRequestException("Product is not available");
        if (product.getStockQuantity() < req.getQuantity())
            throw new BadRequestException("Only " + product.getStockQuantity() + " items in stock");

        cart.getItems().stream()
            .filter(i -> i.getProduct().getId().equals(req.getProductId()))
            .findFirst()
            .ifPresentOrElse(
                existing -> {
                    int newQty = existing.getQuantity() + req.getQuantity();
                    if (newQty > 10) throw new BadRequestException("Maximum 10 items per product");
                    existing.setQuantity(Math.min(newQty, product.getStockQuantity()));
                },
                () -> cart.getItems().add(
                    CartItem.builder()
                        .cart(cart).product(product)
                        .quantity(req.getQuantity())
                        .unitPrice(product.getSellingPrice())
                        .build()
                )
            );

        cartRepository.save(cart);
        return toDto(cart);
    }

    @Transactional
    public CartDto updateQuantity(String email, Long itemId, int quantity) {
        Cart cart = getOrCreateCart(getUser(email));
        CartItem item = cart.getItems().stream()
            .filter(i -> i.getId().equals(itemId)).findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (quantity <= 0) {
            cart.getItems().remove(item);
        } else {
            if (quantity > item.getProduct().getStockQuantity())
                throw new BadRequestException("Not enough stock");
            item.setQuantity(Math.min(quantity, 10));
        }
        cartRepository.save(cart);
        return toDto(cart);
    }

    @Transactional
    public CartDto removeItem(String email, Long itemId) {
        Cart cart = getOrCreateCart(getUser(email));
        cart.getItems().removeIf(i -> i.getId().equals(itemId));
        cartRepository.save(cart);
        return toDto(cart);
    }

    @Transactional
    public void clearCart(String email) {
        Cart cart = getOrCreateCart(getUser(email));
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    // ── helpers ──────────────────────────────────────────────────────

    private User getUser(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId())
            .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));
    }

    private CartDto toDto(Cart cart) {
        List<CartItemDto> items = cart.getItems().stream().map(ci -> {
            Product p = ci.getProduct();
            List<ProductImage> imgs = p.getImages() != null ? p.getImages() : Collections.emptyList();
            String image = imgs.stream()
                .filter(ProductImage::isPrimary).findFirst()
                .map(ProductImage::getImageUrl)
                .orElseGet(() -> imgs.isEmpty() ? null : imgs.get(0).getImageUrl());

            return CartItemDto.builder()
                .itemId(ci.getId())
                .productId(p.getId())
                .productName(p.getName())
                .productImage(image)
                .brand(p.getBrand())
                .quantity(ci.getQuantity())
                .unitPrice(ci.getUnitPrice())
                .originalPrice(p.getOriginalPrice())
                .totalPrice(ci.getTotalPrice())
                .stockAvailable(p.getStockQuantity())
                .build();
        }).toList();

        BigDecimal subtotal = items.stream()
            .map(CartItemDto::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal originalTotal = items.stream()
            .map(i -> i.getOriginalPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal delivery = subtotal.compareTo(new BigDecimal("499")) >= 0
            ? BigDecimal.ZERO : new BigDecimal("49");

        return CartDto.builder()
            .cartId(cart.getId())
            .items(items)
            .totalItems(items.stream().mapToInt(CartItemDto::getQuantity).sum())
            .subtotal(subtotal)
            .deliveryCharges(delivery)
            .total(subtotal.add(delivery))
            .savings(originalTotal.subtract(subtotal))
            .build();
    }
}
