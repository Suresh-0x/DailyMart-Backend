package com.dailymart.service;

import com.dailymart.dto.*;
import com.dailymart.entity.*;
import com.dailymart.entity.Role.ERole;
import com.dailymart.exception.BadRequestException;
import com.dailymart.exception.ResourceNotFoundException;
import com.dailymart.repository.*;
import com.dailymart.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private CartRepository cartRepository;
    @Mock private JwtService jwtService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authManager;
    @Mock private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private Role userRole;

    @BeforeEach
    void setUp() {
        userRole = new Role(1L, ERole.ROLE_USER);
        testUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password("encoded_pass")
                .phone("9876543210")
                .enabled(true)
                .accountNonLocked(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .roles(Set.of(userRole))
                .build();
    }

    @Test
    void register_Success() {
        RegisterRequest req = RegisterRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password("Password@123")
                .phone("9876543210")
                .build();

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password@123")).thenReturn("encoded_pass");
        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        MessageResponse response = authService.register(req);

        assertNotNull(response);
        assertTrue(response.getMessage().contains("successful"));
        verify(emailService, times(1)).sendVerificationEmail(anyString(), anyString());
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void register_DuplicateEmail_ThrowsBadRequest() {
        RegisterRequest req = RegisterRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("existing@example.com")
                .password("Password@123")
                .build();

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(req));
    }

    @Test
    void login_Success() {
        LoginRequest req = new LoginRequest("john@example.com", "Password@123");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(testUser)).thenReturn("jwt_mock_token");

        AuthResponse response = authService.login(req);

        assertNotNull(response);
        assertEquals("jwt_mock_token", response.getToken());
        assertEquals("john@example.com", response.getEmail());
        assertEquals("Bearer", response.getTokenType());
    }

    @Test
    void login_UnverifiedEmail_ThrowsBadRequest() {
        testUser.setEnabled(false);
        LoginRequest req = new LoginRequest("john@example.com", "Password@123");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        assertThrows(BadRequestException.class, () -> authService.login(req));
    }

    @Test
    void verifyEmail_ValidToken_EnablesUser() {
        testUser.setEnabled(false);
        testUser.setEmailVerificationToken("valid_token");

        when(userRepository.findByEmailVerificationToken("valid_token")).thenReturn(Optional.of(testUser));

        MessageResponse res = authService.verifyEmail("valid_token");

        assertTrue(testUser.isEnabled());
        assertNull(testUser.getEmailVerificationToken());
        assertTrue(res.getMessage().contains("verified"));
    }

    @Test
    void resetPassword_ValidToken_Success() {
        testUser.setPasswordResetToken("reset_token");
        testUser.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(1));

        when(userRepository.findByPasswordResetToken("reset_token")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("NewPassword@123")).thenReturn("new_encoded_pass");

        MessageResponse res = authService.resetPassword("reset_token", "NewPassword@123");

        assertEquals("new_encoded_pass", testUser.getPassword());
        assertNull(testUser.getPasswordResetToken());
        assertTrue(res.getMessage().contains("successfully"));
    }
}
