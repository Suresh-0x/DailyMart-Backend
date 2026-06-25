package com.dailymart.service;

import com.dailymart.dto.*;
import com.dailymart.entity.*;
import com.dailymart.exception.*;
import com.dailymart.repository.*;
import com.razorpay.RazorpayClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Value("${razorpay.key-id}")     private String razorpayKeyId;
    @Value("${razorpay.key-secret}") private String razorpayKeySecret;

    @Transactional
    public RazorpayOrderResponse createRazorpayOrder(String orderNumber) throws Exception {
        Order order = orderRepository.findByOrderNumber(orderNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
        long amountPaise = order.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue();

        JSONObject options = new JSONObject();
        options.put("amount", amountPaise);
        options.put("currency", "INR");
        options.put("receipt", orderNumber);

        com.razorpay.Order rzpOrder = client.orders.create(options);
        String rzpOrderId = rzpOrder.get("id");

        paymentRepository.save(Payment.builder()
            .order(order).razorpayOrderId(rzpOrderId).paymentMethod("RAZORPAY")
            .amount(order.getTotalAmount()).currency("INR")
            .status(Payment.PaymentStatus.INITIATED).build());

        return RazorpayOrderResponse.builder()
            .razorpayOrderId(rzpOrderId).currency("INR")
            .amountInPaise(amountPaise).keyId(razorpayKeyId).orderNumber(orderNumber).build();
    }

    @Transactional
    public MessageResponse verifyPayment(PaymentVerifyRequest req) {
        String data = req.getRazorpayOrderId() + "|" + req.getRazorpayPaymentId();
        boolean valid = verifySignature(data, req.getRazorpaySignature());

        Order order = orderRepository.findByOrderNumber(req.getOrderNumber())
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        Payment payment = paymentRepository.findByOrder(order)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (valid) {
            payment.setRazorpayPaymentId(req.getRazorpayPaymentId());
            payment.setRazorpaySignature(req.getRazorpaySignature());
            payment.setStatus(Payment.PaymentStatus.SUCCESS);
            payment.setPaidAt(LocalDateTime.now());
            order.setPaymentStatus(Order.PaymentStatus.PAID);
            order.setOrderStatus(Order.OrderStatus.CONFIRMED);
        } else {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            order.setPaymentStatus(Order.PaymentStatus.FAILED);
        }
        paymentRepository.save(payment);
        orderRepository.save(order);
        return new MessageResponse(valid ? "Payment verified successfully" : "Payment verification failed");
    }

    private boolean verifySignature(String data, String signature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(razorpayKeySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash).equals(signature);
        } catch (Exception e) {
            log.error("Signature verification error", e);
            return false;
        }
    }
}
