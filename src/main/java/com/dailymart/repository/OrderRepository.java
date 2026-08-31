package com.dailymart.repository;

import com.dailymart.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Optional<Order> findByOrderNumber(String orderNumber);

    @Query("SELECT COUNT(o) FROM Order o")
    long countAllOrders();

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.paymentStatus = :status")
    BigDecimal sumRevenueByPaymentStatus(@Param("status") Order.PaymentStatus status);

    default BigDecimal sumRevenue() {
        return sumRevenueByPaymentStatus(Order.PaymentStatus.PAID);
    }

    long countByOrderStatus(Order.OrderStatus orderStatus);

    long countByPaymentStatus(Order.PaymentStatus paymentStatus);

    List<Order> findTop5ByOrderByCreatedAtDesc();
}
