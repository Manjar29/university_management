package com.university.management.service;

import com.university.management.dto.AuthRequest;
import com.university.management.dto.AuthResponse;
import com.university.management.entity.Role;
import com.university.management.entity.User;
import com.university.management.repository.UserRepository;
import com.university.management.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserRepository userRepository;

    private AuthService authService;
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        setJwtField("secret", "this-is-a-very-long-secret-key-for-jwt-signing-1234567890");
        setJwtField("expirationMs", 3600000L);
        authService = new AuthService(authenticationManager, jwtService, userRepository);
    }

    @Test
    void loginShouldReturnTokenForUser() {
        AuthRequest request = new AuthRequest();
        request.setEmail("student@example.com");
        request.setPassword("secret123");

        Authentication authentication = new UsernamePasswordAuthenticationToken("student@example.com", "secret123");
        User user = new User();
        user.setEmail("student@example.com");
        user.setRole(Role.STUDENT);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(user));

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertFalse(response.getToken().isBlank());
        assertEquals(Role.STUDENT.name(), response.getRole());
    }

    private void setJwtField(String fieldName, Object value) {
        try {
            Field field = JwtService.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(jwtService, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException("Failed to configure JwtService for tests", e);
        }
    }
}
