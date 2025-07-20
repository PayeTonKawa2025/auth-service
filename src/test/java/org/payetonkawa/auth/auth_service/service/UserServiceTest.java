package org.payetonkawa.auth.auth_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.payetonkawa.auth.auth_service.model.User;
import org.payetonkawa.auth.auth_service.repository.UserRepository;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class UserServiceTest {

    @Mock private UserRepository repo;

    @InjectMocks
    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(repo);
    }

    @Test
    void findById_shouldReturnOptional() {
        when(repo.findById(1L)).thenReturn(Optional.of(new User()));
        assertTrue(service.findById(1L).isPresent());
    }

    @Test
    void create_shouldSaveUser() {
        User user = new User();
        user.setEmail("x");
        user.setPassword("y");
        when(repo.save(any())).thenReturn(user);

        assertEquals(user, service.create(user));
    }

    @Test
    void delete_shouldDelete() {
        when(repo.existsById(1L)).thenReturn(true);
        assertTrue(service.delete(1L));
    }
}
