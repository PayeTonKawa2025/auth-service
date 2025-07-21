package org.payetonkawa.auth.auth_service.controller;

import jakarta.validation.Valid;
import org.payetonkawa.auth.auth_service.dto.LoginRequest;
import org.payetonkawa.auth.auth_service.dto.RegisterRequest;
import org.payetonkawa.auth.auth_service.dto.UserProfileResponse;
import org.payetonkawa.auth.auth_service.model.User;
import org.payetonkawa.auth.auth_service.service.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest req) {
        if (authService.findByEmail(req.email()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        User user = new User();
        user.setEmail(req.email());
        user.setFirstName(req.firstName());
        user.setLastName(req.lastName());
        user.setPassword(req.password());

        // Affecter le rôle par défaut USER
        user.setRoles(Set.of(authService.getDefaultUserRole()));

        authService.register(user);
        return ResponseEntity.ok("User registered");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest req) {
        var opt = authService.findByEmail(req.email());
        if (opt.isEmpty() || !authService.checkPassword(opt.get(), req.password())) {
            return ResponseEntity.badRequest().body("Invalid credentials");
        }

        String accessToken = authService.generateAccessToken(opt.get());
        String refreshToken = authService.generateRefreshToken(opt.get());

        ResponseCookie accessCookie = ResponseCookie.from("access_token", accessToken)
                .httpOnly(true).secure(false).sameSite("Strict").path("/").maxAge(900).build();

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true).secure(false).sameSite("Strict").path("/").maxAge(86400).build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString(), refreshCookie.toString())
                .body("Login OK");
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@CookieValue(name = "refresh_token", required = false) String token) {
        if (token == null || !authService.validateToken(token)) {
            return ResponseEntity.badRequest().body("Invalid refresh token");
        }

        String email = authService.extractEmailFromToken(token);
        String newAccessToken = authService.generateAccessToken(authService.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found")));

        ResponseCookie newAccessCookie = ResponseCookie.from("access_token", newAccessToken)
                .httpOnly(true).secure(false).sameSite("Strict").path("/").maxAge(900).build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, newAccessCookie.toString())
                .body("Token refreshed");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        ResponseCookie expiredAccess = ResponseCookie.from("access_token", "")
                .httpOnly(true).secure(false).sameSite("Strict").path("/").maxAge(0).build();

        ResponseCookie expiredRefresh = ResponseCookie.from("refresh_token", "")
                .httpOnly(true).secure(false).sameSite("Strict").path("/").maxAge(0).build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, expiredAccess.toString(), expiredRefresh.toString())
                .body("Logout OK");
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@CookieValue(name = "access_token", required = false) String token) {
        if (token == null || !authService.validateToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        String email = authService.extractEmailFromToken(token);
        var optionalUser = authService.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        User user = optionalUser.get();

        // Gestion du rôle : si plusieurs rôles, prends le premier (à adapter selon le besoin)
        String role = user.getRoles().stream()
                .findFirst()
                .map(r -> r.getName())
                .orElse("UNKNOWN");

        UserProfileResponse response = new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                role
        );

        return ResponseEntity.ok(response);
    }


}
