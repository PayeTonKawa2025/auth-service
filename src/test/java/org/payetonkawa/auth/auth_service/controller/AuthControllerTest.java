package org.payetonkawa.auth.auth_service.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.payetonkawa.auth.auth_service.model.Role;
import org.payetonkawa.auth.auth_service.model.User;
import org.payetonkawa.auth.auth_service.service.AuthService;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
@WebMvcTest(controllers = AuthController.class)
public class AuthControllerTest {

    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String FAKE_TOKEN = "fakeToken";

    @BeforeEach
    void setup() {
        AuthController controller = new AuthController(authService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .build();  // Pas de CSRF ici, pas de filtres de sécurité.
    }

    @Test
    void register_userOk_shouldReturn200() throws Exception {
        when(authService.findByEmail("test@example.com")).thenReturn(Optional.empty());

        Role defaultRole = new Role();
        defaultRole.setName("USER");
        when(authService.getDefaultUserRole()).thenReturn(defaultRole);

        String jsonBody = """
            {
              "email": "test@example.com",
              "password": "pass123",
              "firstName": "John",
              "lastName": "Doe"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("User registered"));

        verify(authService).register(argThat(user ->
                user.getEmail().equals("test@example.com") &&
                        user.getRoles().stream().anyMatch(r -> r.getName().equals("USER"))
        ));
    }


    @Test
    void register_emailExists_shouldReturn400() throws Exception {
        when(authService.findByEmail("test@example.com")).thenReturn(Optional.of(new User()));

        String jsonBody = """
                {
                  "email": "test@example.com",
                  "password": "pass123",
                  "firstName": "John",
                  "lastName": "Doe"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email already exists"));
    }

    @Test
    void login_validCredentials_shouldReturn200() throws Exception {
        User mockUser = new User();
        mockUser.setEmail("test@example.com");
        mockUser.setPassword("encodedPassword");

        when(authService.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(authService.checkPassword(any(), eq("pass123"))).thenReturn(true);
        when(authService.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(authService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");

        String jsonBody = """
                {
                  "email": "test@example.com",
                  "password": "pass123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().exists("Set-Cookie"))
                .andExpect(content().string("Login OK"));
    }

    @Test
    void login_invalidCredentials_shouldReturn400() throws Exception {
        when(authService.findByEmail("test@example.com")).thenReturn(Optional.empty());

        String jsonBody = """
                {
                  "email": "test@example.com",
                  "password": "wrongpass"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid credentials"));
    }

    @Test
    void refreshToken_valid_shouldReturn200() throws Exception {
        when(authService.validateToken(FAKE_TOKEN)).thenReturn(true);
        when(authService.extractEmailFromToken(FAKE_TOKEN)).thenReturn("test@example.com");
        when(authService.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(authService.findByEmail("test@example.com"))
                .thenReturn(Optional.of(new User()));  // Corrigé



        mockMvc.perform(post("/api/auth/refresh-token")
                        .cookie(new jakarta.servlet.http.Cookie("refresh_token", FAKE_TOKEN)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().exists("Set-Cookie"))
                .andExpect(content().string("Token refreshed"));
    }

    @Test
    void refreshToken_invalid_shouldReturn400() throws Exception {
        when(authService.validateToken(FAKE_TOKEN)).thenReturn(false);

        mockMvc.perform(post("/api/auth/refresh-token")
                        .cookie(new jakarta.servlet.http.Cookie("refresh_token", FAKE_TOKEN)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid refresh token"));
    }

    @Test
    void logout_shouldReturn200() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().exists("Set-Cookie"))
                .andExpect(content().string("Logout OK"));
    }

    @Test
    void me_validToken_shouldReturnUserInfo() throws Exception {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");
        mockUser.setFirstName("John");
        mockUser.setLastName("Doe");

        when(authService.validateToken(FAKE_TOKEN)).thenReturn(true);
        when(authService.extractEmailFromToken(FAKE_TOKEN)).thenReturn("test@example.com");
        when(authService.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));

        mockMvc.perform(get("/api/auth/me")
                        .cookie(new jakarta.servlet.http.Cookie("access_token", FAKE_TOKEN)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstname").value("John"))
                .andExpect(jsonPath("$.lastname").value("Doe"));
    }

    @Test
    void me_invalidToken_shouldReturn401() throws Exception {
        when(authService.validateToken(FAKE_TOKEN)).thenReturn(false);

        mockMvc.perform(get("/api/auth/me")
                        .cookie(new jakarta.servlet.http.Cookie("access_token", FAKE_TOKEN)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}
