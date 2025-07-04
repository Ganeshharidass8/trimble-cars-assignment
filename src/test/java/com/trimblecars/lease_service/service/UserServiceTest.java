package com.trimblecars.lease_service.service;

import com.trimblecars.lease_service.entity.User;
import com.trimblecars.lease_service.enums.UserRole;
import com.trimblecars.lease_service.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    // ✅ Test 1: Register user successfully
    @Test
    @DisplayName("Should register user successfully when email is unique")
    void shouldRegisterUserSuccessfully() {
        User input = new User(null, "John", "john@example.com", UserRole.CUSTOMER);
        User saved = new User(1L, "John", "john@example.com", UserRole.CUSTOMER);

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(input)).thenReturn(saved);

        User result = userService.registerUser(input);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John", result.getName());
        assertEquals(UserRole.CUSTOMER, result.getRole());
        verify(userRepository).save(input);
    }


    // ❌ Test 3: Null input
    @Test
    @DisplayName("Should throw exception when user is null")
    void shouldThrowExceptionWhenUserIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.registerUser(null)
        );

        assertEquals("User cannot be null", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    // ✅ Test 4: Lookup by email
    @Test
    @DisplayName("Should find user by email")
    void shouldFindUserByEmail() {
        User expected = new User(5L, "Ayesha", "ayesha@trimble.com", UserRole.OWNER);
        when(userRepository.findByEmail("ayesha@trimble.com")).thenReturn(Optional.of(expected));

        Optional<User> result = userRepository.findByEmail("ayesha@trimble.com");

        assertTrue(result.isPresent());
        assertEquals("Ayesha", result.get().getName());
        assertEquals(UserRole.OWNER, result.get().getRole());
    }

    // ✅ Test 5: Email not found
    @Test
    @DisplayName("Should return empty when email is not found")
    void shouldReturnEmptyWhenEmailNotFound() {
        when(userRepository.findByEmail("notfound@trimble.com")).thenReturn(Optional.empty());

        Optional<User> result = userRepository.findByEmail("notfound@trimble.com");

        assertTrue(result.isEmpty());
    }
}
