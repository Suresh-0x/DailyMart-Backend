package com.dailymart.service;

import com.dailymart.dto.*;
import com.dailymart.entity.Coupon;
import com.dailymart.exception.BadRequestException;
import com.dailymart.exception.ResourceNotFoundException;
import com.dailymart.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    public CouponDto validateAndApplyCoupon(String code, BigDecimal subtotal) {
        if (code == null || code.trim().isEmpty()) {
            throw new BadRequestException("Coupon code cannot be empty");
        }

        Coupon coupon = couponRepository.findByCodeIgnoreCaseAndActiveTrue(code.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or inactive coupon code: " + code));

        if (coupon.getExpiryDate() != null && coupon.getExpiryDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Coupon has expired");
        }

        if (coupon.getUsageLimit() != null && coupon.getUsageCount() >= coupon.getUsageLimit()) {
            throw new BadRequestException("Coupon usage limit has been reached");
        }

        if (coupon.getMinOrderAmount() != null && subtotal.compareTo(coupon.getMinOrderAmount()) < 0) {
            throw new BadRequestException("Minimum order amount of ₹" + coupon.getMinOrderAmount() + " required to use this coupon");
        }

        BigDecimal discountAmount;
        if (coupon.getDiscountType() == Coupon.DiscountType.PERCENTAGE) {
            discountAmount = subtotal.multiply(coupon.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (coupon.getMaxDiscountAmount() != null && discountAmount.compareTo(coupon.getMaxDiscountAmount()) > 0) {
                discountAmount = coupon.getMaxDiscountAmount();
            }
        } else {
            discountAmount = coupon.getDiscountValue();
        }

        if (discountAmount.compareTo(subtotal) > 0) {
            discountAmount = subtotal;
        }

        BigDecimal finalAmount = subtotal.subtract(discountAmount);

        return CouponDto.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .description(coupon.getDescription())
                .discountType(coupon.getDiscountType().name())
                .discountValue(coupon.getDiscountValue())
                .minOrderAmount(coupon.getMinOrderAmount())
                .maxDiscountAmount(coupon.getMaxDiscountAmount())
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .expiryDate(coupon.getExpiryDate())
                .usageLimit(coupon.getUsageLimit())
                .usageCount(coupon.getUsageCount())
                .active(coupon.isActive())
                .build();
    }

    @Transactional
    public void incrementUsage(String code) {
        couponRepository.findByCodeIgnoreCase(code).ifPresent(c -> {
            c.setUsageCount(c.getUsageCount() + 1);
            couponRepository.save(c);
        });
    }

    public List<CouponDto> getActiveCoupons() {
        return couponRepository.findByActiveTrueOrderByCreatedAtDesc().stream()
                .filter(c -> c.getExpiryDate() == null || !c.getExpiryDate().isBefore(LocalDate.now()))
                .filter(c -> c.getUsageLimit() == null || c.getUsageCount() < c.getUsageLimit())
                .map(this::toDto)
                .toList();
    }

    public List<CouponDto> getAllCoupons() {
        return couponRepository.findAll().stream().map(this::toDto).toList();
    }

    public CouponDto getCouponById(Long id) {
        return toDto(couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with id: " + id)));
    }

    @Transactional
    public CouponDto createCoupon(CreateCouponRequest req) {
        if (couponRepository.existsByCodeIgnoreCase(req.getCode())) {
            throw new BadRequestException("Coupon code already exists: " + req.getCode());
        }

        Coupon coupon = Coupon.builder()
                .code(req.getCode().toUpperCase().trim())
                .description(req.getDescription())
                .discountType(Coupon.DiscountType.valueOf(req.getDiscountType().toUpperCase()))
                .discountValue(req.getDiscountValue())
                .minOrderAmount(req.getMinOrderAmount() != null ? req.getMinOrderAmount() : BigDecimal.ZERO)
                .maxDiscountAmount(req.getMaxDiscountAmount())
                .expiryDate(req.getExpiryDate())
                .usageLimit(req.getUsageLimit() != null ? req.getUsageLimit() : 1000)
                .usageCount(0)
                .active(req.getActive() != null ? req.getActive() : true)
                .build();

        return toDto(couponRepository.save(coupon));
    }

    @Transactional
    public CouponDto updateCoupon(Long id, CreateCouponRequest req) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with id: " + id));

        if (req.getCode() != null && !req.getCode().equalsIgnoreCase(coupon.getCode())) {
            if (couponRepository.existsByCodeIgnoreCase(req.getCode())) {
                throw new BadRequestException("Coupon code already exists: " + req.getCode());
            }
            coupon.setCode(req.getCode().toUpperCase().trim());
        }

        if (req.getDescription() != null)       coupon.setDescription(req.getDescription());
        if (req.getDiscountType() != null)     coupon.setDiscountType(Coupon.DiscountType.valueOf(req.getDiscountType().toUpperCase()));
        if (req.getDiscountValue() != null)    coupon.setDiscountValue(req.getDiscountValue());
        if (req.getMinOrderAmount() != null)   coupon.setMinOrderAmount(req.getMinOrderAmount());
        if (req.getMaxDiscountAmount() != null) coupon.setMaxDiscountAmount(req.getMaxDiscountAmount());
        if (req.getExpiryDate() != null)       coupon.setExpiryDate(req.getExpiryDate());
        if (req.getUsageLimit() != null)       coupon.setUsageLimit(req.getUsageLimit());
        if (req.getActive() != null)           coupon.setActive(req.getActive());

        return toDto(couponRepository.save(coupon));
    }

    @Transactional
    public void deleteCoupon(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with id: " + id));
        coupon.setActive(false);
        couponRepository.save(coupon);
    }

    private CouponDto toDto(Coupon c) {
        return CouponDto.builder()
                .id(c.getId())
                .code(c.getCode())
                .description(c.getDescription())
                .discountType(c.getDiscountType().name())
                .discountValue(c.getDiscountValue())
                .minOrderAmount(c.getMinOrderAmount())
                .maxDiscountAmount(c.getMaxDiscountAmount())
                .expiryDate(c.getExpiryDate())
                .usageLimit(c.getUsageLimit())
                .usageCount(c.getUsageCount())
                .active(c.isActive())
                .build();
    }
}
