package com.dailymart.controller;

import com.dailymart.dto.*;
import com.dailymart.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/razorpay/create/{orderNumber}")
    public ResponseEntity<RazorpayOrderResponse> createRazorpayOrder(
            @PathVariable String orderNumber) throws Exception {
        return ResponseEntity.ok(paymentService.createRazorpayOrder(orderNumber));
    }

    @PostMapping("/razorpay/verify")
    public ResponseEntity<MessageResponse> verifyPayment(@RequestBody PaymentVerifyRequest req) {
        return ResponseEntity.ok(paymentService.verifyPayment(req));
    }
}
