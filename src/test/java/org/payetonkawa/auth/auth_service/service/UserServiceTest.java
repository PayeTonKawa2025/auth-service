package org.payetonkawa.auth.auth_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.payetonkawa.auth.auth_service.dto.UpdateUserRequest;
import org.payetonkawa.auth.auth_service.model.User;
import org.payetonkawa.auth.auth_service.repository.RoleRepository;
import org.payetonkawa.auth.auth_service.repository.UserRepository;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class UserServiceTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private RoleRepository roleRepo;

    @InjectMocks
    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(userRepo, roleRepo);
    }

    @Test
    void findById_shouldReturnOptional() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(new User()));
        assertTrue(service.findById(1L).isPresent());
    }

    @Test
    void create_shouldSaveUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("securePassword");

        when(userRepo.save(any())).thenReturn(user);

        assertEquals(user, service.create(user));
    }

    @Test
    void delete_shouldDelete() {
        when(userRepo.existsById(1L)).thenReturn(true);
        assertTrue(service.delete(1L));
    }

    @Test
    void delete_shouldReturnFalseIfNotExists() {
        when(userRepo.existsById(1L)).thenReturn(false);
        assertFalse(service.delete(1L));
    }

    @Test
    void update_shouldUpdateUser_withoutRoleAndStatus() {
        User user = new User();
        user.setRoles(Collections.emptySet());

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(userRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateUserRequest dto = new UpdateUserRequest("First", "Last", "email@example.com", null, null);

        Optional<User> result = service.update(1L, dto);

        assertTrue(result.isPresent());
        assertEquals("First", result.get().getFirstName());
    }

    @Test
    void update_shouldThrow_whenRoleNotFound() {
        User user = new User();
        user.setRoles(Collections.emptySet());

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepo.findByName("MANAGER")).thenReturn(Optional.empty());

        // ⚠️ ordre des champs : firstName, lastName, email, status, role
        UpdateUserRequest dto = new UpdateUserRequest(
                "A", "B", "email@example.com",
                "manager",    // rôle inexistant => on teste qu'il n'existe pas
                "ACTIVE"      // statut valide => OK
        );


        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.update(1L, dto));
        assertEquals("Role not found: manager", ex.getMessage());
    }

}
