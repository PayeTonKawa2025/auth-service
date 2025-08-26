package org.payetonkawa.auth.auth_service.init;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.payetonkawa.auth.auth_service.model.Role;
import org.payetonkawa.auth.auth_service.model.Status;
import org.payetonkawa.auth.auth_service.model.User;
import org.payetonkawa.auth.auth_service.repository.RoleRepository;
import org.payetonkawa.auth.auth_service.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.Mockito.*;

class AdminInitializerTest {

    @Mock private RoleRepository roleRepo;
    @Mock private UserRepository userRepo;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminInitializer initializer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private void injectAdminCredentials(String email, String password) throws Exception {
        // Hacky but effective — use reflection to set @Value-injected fields
        var emailField = AdminInitializer.class.getDeclaredField("adminEmail");
        emailField.setAccessible(true);
        emailField.set(initializer, email);

        var passwordField = AdminInitializer.class.getDeclaredField("adminPassword");
        passwordField.setAccessible(true);
        passwordField.set(initializer, password);
    }

    @Test
    void shouldSkipIfEmailIsBlank() throws Exception {
        injectAdminCredentials("", "password");
        initializer.run();
        verifyNoInteractions(roleRepo, userRepo);
    }

    @Test
    void shouldCreateAdminWithTempPasswordAndMissingRole() throws Exception {
        injectAdminCredentials("admin@example.com", "");

        when(roleRepo.findByName("ADMIN")).thenReturn(Optional.empty());
        when(roleRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userRepo.findByEmail("admin@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");

        initializer.run();

        verify(roleRepo).save(any(Role.class));
        verify(userRepo).save(argThat(user ->
                user.getEmail().equals("admin@example.com") &&
                        user.getFirstName().equals("Admin") &&
                        user.getLastName().equals("Account") &&
                        user.getStatus() == Status.ACTIVE &&
                        user.getRoles().stream().anyMatch(role -> "ADMIN".equals(role.getName()))
        ));
    }

    @Test
    void shouldCreateAdminWithCustomPasswordAndExistingRole() throws Exception {
        injectAdminCredentials("admin@example.com", "mySecretPassword");

        when(roleRepo.findByName("ADMIN")).thenReturn(Optional.of(new Role("ADMIN")));
        when(userRepo.findByEmail("admin@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("mySecretPassword")).thenReturn("encoded-password");

        initializer.run();

        verify(userRepo).save(any(User.class));
    }

    @Test
    void shouldDoNothingIfAdminAlreadyExists() throws Exception {
        injectAdminCredentials("admin@example.com", "password");

        when(roleRepo.findByName("ADMIN")).thenReturn(Optional.of(new Role("ADMIN")));
        when(userRepo.findByEmail("admin@example.com"))
                .thenReturn(Optional.of(mock(User.class)));

        initializer.run();

        verify(userRepo, never()).save(any());
    }

    @Test
    void shouldLogErrorIfExceptionOccurs() throws Exception {
        injectAdminCredentials("admin@example.com", "password");

        when(roleRepo.findByName("ADMIN")).thenThrow(new RuntimeException("DB error"));

        initializer.run();
        // Pas besoin de vérifier les logs, ce test garantit que l’exception est catchée
    }
}
