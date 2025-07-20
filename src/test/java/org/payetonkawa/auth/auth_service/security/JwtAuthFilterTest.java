package org.payetonkawa.auth.auth_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class JwtAuthFilterTest {

    private JwtService jwtService;
    private JwtAuthFilter filter;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        filter = new JwtAuthFilter(jwtService);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);

        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_validToken_setsAuthentication() throws ServletException, IOException {
        Cookie tokenCookie = new Cookie("access_token", "validToken");

        when(request.getCookies()).thenReturn(new Cookie[]{tokenCookie});
        when(jwtService.validateToken("validToken")).thenReturn(true);
        when(jwtService.getEmailFromToken("validToken")).thenReturn("user@example.com");

        filter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("user@example.com", SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        verify(filterChain).doFilter(request, response);

        System.out.print("Filter executed with valid token, authentication set.\n");
    }

    @Test
    void doFilterInternal_invalidToken_noAuthentication() throws ServletException, IOException {
        Cookie tokenCookie = new Cookie("access_token", "invalidToken");

        when(request.getCookies()).thenReturn(new Cookie[]{tokenCookie});
        when(jwtService.validateToken("invalidToken")).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);

        System.out.print("Filter executed with invalid token, no authentication set.\n");
    }

    @Test
    void doFilterInternal_noToken_noAuthentication() throws ServletException, IOException {
        when(request.getCookies()).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);

        System.out.print("Filter executed without token, no authentication set.\n");
    }
}
