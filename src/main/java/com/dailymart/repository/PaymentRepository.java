package com.dailymart.repository;

import com.dailymart.entity.Order;
import com.dailymart.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrder(Order order);
    Optional<Payment> findByRazorpayOrderId(String rzpOrderId);
}
