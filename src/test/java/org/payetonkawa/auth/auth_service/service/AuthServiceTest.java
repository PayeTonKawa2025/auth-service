package org.payetonkawa.auth.auth_service.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.payetonkawa.auth.auth_service.model.Role;
import org.payetonkawa.auth.auth_service.model.User;
import org.payetonkawa.auth.auth_service.repository.RoleRepository;
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
    @Mock private RoleRepository roleRepository;

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
    void register_shouldEncodePasswordAndSaveUser() {
        User user = new User();
        user.setPassword("raw");
        when(encoder.encode("raw")).thenReturn("encoded");

        authService.register(user);

        assertEquals("encoded", user.getPassword());
        verify(repo).save(user);
    }

    @Test
    void generateAccessToken_shouldCallJwtService() {
        User user = new User();
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");

        String token = authService.generateAccessToken(user);

        assertEquals("access-token", token);
    }

    @Test
    void generateRefreshToken_shouldCallJwtService() {
        User user = new User();
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh-token");

        String token = authService.generateRefreshToken(user);

        assertEquals("refresh-token", token);
    }

    @Test
    void getDefaultUserRole_shouldReturnUserRole_whenFound() {
        Role role = new Role();
        role.setName("USER");
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(role));

        Role result = authService.getDefaultUserRole();

        assertEquals("USER", result.getName());
    }

    @Test
    void getDefaultUserRole_shouldThrow_whenNotFound() {
        when(roleRepository.findByName("USER")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.getDefaultUserRole());
        assertTrue(exception.getMessage().contains("Default role USER not found"));
    }

    @Test
    void extractEmailFromToken_shouldCallJwtService() {
        when(jwtService.getEmailFromToken("token")).thenReturn("email@example.com");

        String result = authService.extractEmailFromToken("token");

        assertEquals("email@example.com", result);
    }

    @Test
    void validateToken_shouldReturnTrue() {
        when(jwtService.validateToken("token")).thenReturn(true);

        assertTrue(authService.validateToken("token"));
    }

    @Test
    void save_shouldCallRepoSave() {
        User user = new User();
        authService.save(user);
        verify(repo).save(user);
    }

    @Test
    void encodePassword_shouldReturnEncodedPassword() {
        when(encoder.encode("raw")).thenReturn("encoded");

        String result = authService.encodePassword("raw");

        assertEquals("encoded", result);
    }
}
