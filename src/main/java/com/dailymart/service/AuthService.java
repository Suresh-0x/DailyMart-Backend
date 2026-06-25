package com.dailymart.service;

import com.dailymart.dto.*;
import com.dailymart.entity.*;
import com.dailymart.entity.Role.ERole;
import com.dailymart.exception.*;
import com.dailymart.repository.*;
import com.dailymart.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CartRepository cartRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final EmailService emailService;

    @Transactional
    public MessageResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail()))
            throw new BadRequestException("Email already in use");

        User user = User.builder()
            .firstName(req.getFirstName())
            .lastName(req.getLastName())
            .email(req.getEmail())
            .password(passwordEncoder.encode(req.getPassword()))
            .phone(req.getPhone())
            .emailVerificationToken(UUID.randomUUID().toString())
            .enabled(false)
            .build();

        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
            .orElseThrow(() -> new RuntimeException("Role not found"));
        user.setRoles(Set.of(userRole));
        userRepository.save(user);

        cartRepository.save(Cart.builder().user(user).build());
        emailService.sendVerificationEmail(user.getEmail(), user.getEmailVerificationToken());

        return new MessageResponse("Registration successful! Please verify your email.");
    }

    public AuthResponse login(LoginRequest req) {
        authManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );
        User user = userRepository.findByEmail(req.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!user.isEnabled())
            throw new BadRequestException("Please verify your email first");

        String token = jwtService.generateToken(user);
        return AuthResponse.builder()
            .token(token)
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .roles(user.getRoles().stream().map(r -> r.getName().name()).toList())
            .build();
    }

    @Transactional
    public MessageResponse verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
            .orElseThrow(() -> new BadRequestException("Invalid or expired verification token"));
        user.setEnabled(true);
        user.setEmailVerificationToken(null);
        userRepository.save(user);
        return new MessageResponse("Email verified successfully! You can now login.");
    }

    @Transactional
    public MessageResponse forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("No account found with this email"));
        String token = UUID.randomUUID().toString();
        user.setPasswordResetToken(token);
        user.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);
        emailService.sendPasswordResetEmail(email, token);
        return new MessageResponse("Password reset link sent to your email");
    }

    @Transactional
    public MessageResponse resetPassword(String token, String newPassword) {
        User user = userRepository.findByPasswordResetToken(token)
            .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));
        if (user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now()))
            throw new BadRequestException("Reset token has expired");
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        userRepository.save(user);
        return new MessageResponse("Password reset successfully");
    }
}
