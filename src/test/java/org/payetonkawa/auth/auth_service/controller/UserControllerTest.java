package org.payetonkawa.auth.auth_service.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.payetonkawa.auth.auth_service.model.User;
import org.payetonkawa.auth.auth_service.service.UserService;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)

public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @BeforeEach
    void setup() {
        UserController controller = new UserController(userService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .build(); // Pas de CSRF ici, pas de filtres de sécurité.
    }

    @Test
    void getAll_shouldReturn200() throws Exception {
        Mockito.when(userService.findAll()).thenReturn(List.of(new User()));
        mockMvc.perform(get("/api/auth/users"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void create_shouldReturn201() throws Exception {
        User user = new User();
        user.setId(1L);
        Mockito.when(userService.create(any())).thenReturn(user);

        String body = """
            {
              "email": "john@doe.com",
              "password": "pass123"
            }
            """;

        mockMvc.perform(post("/api/auth/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    void getById_shouldReturn200() throws Exception {
        Mockito.when(userService.findById(1L)).thenReturn(Optional.of(new User()));
        mockMvc.perform(get("/api/auth/users/1"))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
