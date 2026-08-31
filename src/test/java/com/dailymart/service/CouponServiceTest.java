package com.dailymart.service;

import com.dailymart.dto.*;
import com.dailymart.entity.Coupon;
import com.dailymart.exception.BadRequestException;
import com.dailymart.repository.CouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock private CouponRepository couponRepository;

    @InjectMocks
    private CouponService couponService;

    private Coupon percentageCoupon;
    private Coupon flatCoupon;

    @BeforeEach
    void setUp() {
        percentageCoupon = Coupon.builder()
                .id(1L)
                .code("SAVE20")
                .discountType(Coupon.DiscountType.PERCENTAGE)
                .discountValue(new BigDecimal("20.00"))
                .minOrderAmount(new BigDecimal("500.00"))
                .maxDiscountAmount(new BigDecimal("150.00"))
                .expiryDate(LocalDate.now().plusMonths(1))
                .usageLimit(100)
                .usageCount(5)
                .active(true)
                .build();

        flatCoupon = Coupon.builder()
                .id(2L)
                .code("FLAT50")
                .discountType(Coupon.DiscountType.FLAT)
                .discountValue(new BigDecimal("50.00"))
                .minOrderAmount(new BigDecimal("300.00"))
                .expiryDate(LocalDate.now().plusMonths(1))
                .usageLimit(100)
                .usageCount(0)
                .active(true)
                .build();
    }

    @Test
    void validateAndApply_PercentageCoupon_Success() {
        when(couponRepository.findByCodeIgnoreCaseAndActiveTrue("SAVE20"))
                .thenReturn(Optional.of(percentageCoupon));

        CouponDto result = couponService.validateAndApplyCoupon("SAVE20", new BigDecimal("600.00"));

        assertNotNull(result);
        assertEquals(new BigDecimal("120.00"), result.getDiscountAmount()); // 20% of 600
        assertEquals(new BigDecimal("480.00"), result.getFinalAmount());
    }

    @Test
    void validateAndApply_PercentageCoupon_CappedAtMaxDiscount() {
        when(couponRepository.findByCodeIgnoreCaseAndActiveTrue("SAVE20"))
                .thenReturn(Optional.of(percentageCoupon));

        CouponDto result = couponService.validateAndApplyCoupon("SAVE20", new BigDecimal("1000.00"));

        assertNotNull(result);
        assertEquals(new BigDecimal("150.00"), result.getDiscountAmount()); // Capped at max 150
        assertEquals(new BigDecimal("850.00"), result.getFinalAmount());
    }

    @Test
    void validateAndApply_FlatCoupon_Success() {
        when(couponRepository.findByCodeIgnoreCaseAndActiveTrue("FLAT50"))
                .thenReturn(Optional.of(flatCoupon));

        CouponDto result = couponService.validateAndApplyCoupon("FLAT50", new BigDecimal("400.00"));

        assertNotNull(result);
        assertEquals(new BigDecimal("50.00"), result.getDiscountAmount());
        assertEquals(new BigDecimal("350.00"), result.getFinalAmount());
    }

    @Test
    void validateAndApply_BelowMinOrderAmount_ThrowsBadRequest() {
        when(couponRepository.findByCodeIgnoreCaseAndActiveTrue("SAVE20"))
                .thenReturn(Optional.of(percentageCoupon));

        assertThrows(BadRequestException.class,
                () -> couponService.validateAndApplyCoupon("SAVE20", new BigDecimal("400.00")));
    }

    @Test
    void validateAndApply_ExpiredCoupon_ThrowsBadRequest() {
        percentageCoupon.setExpiryDate(LocalDate.now().minusDays(1));
        when(couponRepository.findByCodeIgnoreCaseAndActiveTrue("SAVE20"))
                .thenReturn(Optional.of(percentageCoupon));

        assertThrows(BadRequestException.class,
                () -> couponService.validateAndApplyCoupon("SAVE20", new BigDecimal("600.00")));
    }

    @Test
    void validateAndApply_ExceededUsageLimit_ThrowsBadRequest() {
        percentageCoupon.setUsageLimit(5);
        percentageCoupon.setUsageCount(5);
        when(couponRepository.findByCodeIgnoreCaseAndActiveTrue("SAVE20"))
                .thenReturn(Optional.of(percentageCoupon));

        assertThrows(BadRequestException.class,
                () -> couponService.validateAndApplyCoupon("SAVE20", new BigDecimal("600.00")));
    }
}
