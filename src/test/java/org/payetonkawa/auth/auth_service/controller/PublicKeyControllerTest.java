package org.payetonkawa.auth.auth_service.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.payetonkawa.auth.auth_service.security.JwtService;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PublicKeyController.class)
public class PublicKeyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @BeforeEach
    void setup() {
        PublicKeyController controller = new PublicKeyController(jwtService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .build(); // Pas de CSRF ici, pas de filtres de sécurité.
    }

    @Test
    void getPublicKey_shouldReturn200() throws Exception {
        Mockito.when(jwtService.getEncodedPublicKey()).thenReturn("FAKE_PUBLIC_KEY");

        mockMvc.perform(get("/api/auth/public-key"))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
