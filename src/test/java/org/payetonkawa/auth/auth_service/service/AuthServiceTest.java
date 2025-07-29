package org.payetonkawa.auth.auth_service.service;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.payetonkawa.auth.auth_service.model.User;
import org.payetonkawa.auth.auth_service.repository.UserRepository;
import org.payetonkawa.auth.auth_service.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository repo;
    @Mock private PasswordEncoder encoder;
    @Mock private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void findByEmail_shouldReturnOptional() {
        when(repo.findByEmail("email")).thenReturn(Optional.of(new User()));
        assertTrue(authService.findByEmail("email").isPresent());
    }

    @Test
    void checkPassword_shouldReturnTrue() {
        User user = new User();
        user.setPassword("hashed");
        when(encoder.matches("raw", "hashed")).thenReturn(true);

        assertTrue(authService.checkPassword(user, "raw"));
    }

    @Test
    void register_shouldEncodePassword() {
        User user = new User();
        user.setPassword("raw");
        when(encoder.encode("raw")).thenReturn("encoded");

        authService.register(user);

        assertEquals("encoded", user.getPassword());
        verify(repo).save(user);
    }
}
