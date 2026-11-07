package com.university.management.service;

import com.university.management.dto.UserCreateRequest;
import com.university.management.dto.UserUpdateRequest;
import com.university.management.entity.Role;
import com.university.management.entity.User;
import com.university.management.exception.ConflictException;
import com.university.management.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void createUserHashesPasswordAndSavesUser() {
        UserCreateRequest request = new UserCreateRequest();
        request.setName("Amina Rahman");
        request.setEmail("amina@example.com");
        request.setPassword("password123");
        request.setRole(Role.STUDENT);

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User created = userService.createUser(request);

        assertEquals(request.getName(), created.getName());
        assertEquals(request.getEmail(), created.getEmail());
        assertEquals(Role.STUDENT, created.getRole());
        assertEquals("hashed-password", created.getPasswordHash());
        assertTrue(created.isEnabled());
        verify(userRepository).save(created);
    }

    @Test
    void createUserRejectsDuplicateEmail() {
        UserCreateRequest request = new UserCreateRequest();
        request.setEmail("existing@example.com");
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.createUser(request));
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void updateUserChangesOnlyProvidedFields() {
        User existing = new User();
        existing.setId(7L);
        existing.setName("Old Name");
        existing.setEmail("old@example.com");
        existing.setPhone("0100");

        UserUpdateRequest request = new UserUpdateRequest();
        request.setName("New Name");
        request.setPhone("0200");

        when(userRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);

        User updated = userService.updateUser(7L, request);

        assertEquals("New Name", updated.getName());
        assertEquals("old@example.com", updated.getEmail());
        assertEquals("0200", updated.getPhone());
        verify(userRepository).save(existing);
        verify(userRepository, never()).existsByEmail(anyString());
    }
}
