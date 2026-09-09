package com.quizapp.service;

import com.quizapp.model.User;
import com.quizapp.model.UserRole;
import com.quizapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AuthenticationService}.
 */
@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    @Mock private UserRepository userRepo;
    @InjectMocks private AuthenticationService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(1);
        sampleUser.setName("Alice");
        sampleUser.setEmail("alice@test.com");
        sampleUser.setPassword("password123");
        sampleUser.setRole(UserRole.STUDENT);
    }

    @Test
    void testLoginSuccess() {
        when(userRepo.findByEmailAndPassword("alice@test.com", "password123"))
                .thenReturn(Optional.of(sampleUser));

        Optional<User> result = authService.login("alice@test.com", "password123");
        assertTrue(result.isPresent());
        assertEquals("Alice", result.get().getName());
        assertTrue(authService.isLoggedIn());
        assertTrue(authService.isStudent());
        assertFalse(authService.isTeacher());
    }

    @Test
    void testLoginInvalidCredentials() {
        when(userRepo.findByEmailAndPassword("alice@test.com", "wrongpass"))
                .thenReturn(Optional.empty());

        Optional<User> result = authService.login("alice@test.com", "wrongpass");
        assertTrue(result.isEmpty());
        assertFalse(authService.isLoggedIn());
    }

    @Test
    void testLogoutClearsUser() {
        when(userRepo.findByEmailAndPassword("alice@test.com", "password123"))
                .thenReturn(Optional.of(sampleUser));

        authService.login("alice@test.com", "password123");
        assertTrue(authService.isLoggedIn());

        authService.logout();
        assertFalse(authService.isLoggedIn());
        assertTrue(authService.getCurrentUser().isEmpty());
    }

    @Test
    void testRegisterNewStudentSuccess() {
        when(userRepo.emailExists("bob@test.com")).thenReturn(false);
        when(userRepo.save(any(User.class))).thenReturn(2);

        User registered = authService.register("Bob", "bob@test.com", "pass123", UserRole.STUDENT);
        assertNotNull(registered);
        assertEquals(2, registered.getId());
        assertEquals("Bob", registered.getName());
        verify(userRepo).save(any(User.class));
    }

    @Test
    void testRegisterDuplicateEmailThrows() {
        when(userRepo.emailExists("alice@test.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                authService.register("Alice Clone", "alice@test.com", "pass123", UserRole.STUDENT));
    }
}
