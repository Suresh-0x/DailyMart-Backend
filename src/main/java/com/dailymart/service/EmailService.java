package com.dailymart.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Async
    public void sendVerificationEmail(String to, String token) {
        String link = frontendUrl + "/verify-email?token=" + token;
        String html = """
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:32px">
                <h1 style="color:#FF6161">🛍️ DailyMart</h1>
                <h2>Verify Your Email</h2>
                <p>Thank you for registering! Please click below to verify your email.</p>
                <a href="%s" style="background:#FF6161;color:#fff;padding:14px 32px;border-radius:10px;text-decoration:none;font-weight:700">
                    Verify Email ✓
                </a>
                <p style="color:#888;font-size:13px;margin-top:24px">Link expires in 24 hours.</p>
            </div>
        """.formatted(link);
        sendHtmlEmail(to, "Verify Your DailyMart Account", html);
    }

    @Async
    public void sendPasswordResetEmail(String to, String token) {
        String link = frontendUrl + "/reset-password?token=" + token;
        String html = """
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:32px">
                <h1 style="color:#FF6161">🛍️ DailyMart</h1>
                <h2>Reset Your Password</h2>
                <p>Click below to reset your password:</p>
                <a href="%s" style="background:#FF6161;color:#fff;padding:14px 32px;border-radius:10px;text-decoration:none;font-weight:700">
                    Reset Password 🔒
                </a>
                <p style="color:#888;font-size:13px;margin-top:24px">⚠️ This link expires in 1 hour.</p>
            </div>
        """.formatted(link);
        sendHtmlEmail(to, "Reset Your DailyMart Password", html);
    }

    @Async
    public void sendOrderConfirmation(String to, String orderNumber, String total) {
        String html = """
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:32px">
                <h1 style="color:#FF6161">🛍️ DailyMart</h1>
                <h2 style="color:#065F46">✅ Order Confirmed!</h2>
                <p>Your order has been placed successfully.</p>
                <p><strong>Order Number:</strong> %s</p>
                <p><strong>Total Amount:</strong> %s</p>
                <p><strong>Estimated Delivery:</strong> 3-5 Business Days</p>
            </div>
        """.formatted(orderNumber, total);
        sendHtmlEmail(to, "Order Confirmed - " + orderNumber, html);
    }

    private void sendHtmlEmail(String to, String subject, String html) {
        try {
            var message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("Email sent to {}: {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
