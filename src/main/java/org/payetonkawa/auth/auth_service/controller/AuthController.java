package org.payetonkawa.auth.auth_service.controller;

import jakarta.validation.Valid;
import org.payetonkawa.auth.auth_service.dto.*;
import org.payetonkawa.auth.auth_service.model.Status;
import org.payetonkawa.auth.auth_service.model.User;
import org.payetonkawa.auth.auth_service.model.Role;
import org.payetonkawa.auth.auth_service.service.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("api/auth")
public class AuthController {

    private final AuthService authService;
    @Value("${security.cookie.secure:false}") // fallback false si non défini
    private boolean secureCookies;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid RegisterRequest req) {
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
        user.setStatus(Status.ACTIVE); // Assurez-vous que le statut est actif par défaut
        user.setCreatedAt(LocalDateTime.now());
        user.setLastLogin(LocalDateTime.now()); // Initialiser lastLogin
        // Enregistrer l'utilisateur


        authService.register(user);
        return ResponseEntity.ok("User registered");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody @Valid LoginRequest req) {
        var opt = authService.findByEmail(req.email());

        if (opt.isEmpty() || !authService.checkPassword(opt.get(), req.password())) {
            return ResponseEntity.badRequest().body("Identifiants invalides");
        }

        User user = opt.get();

        // 🚫 Bloquer si inactif
        if (user.getStatus() != Status.ACTIVE) {
            return ResponseEntity.status(403).body("Votre compte est inactif. Veuillez contacter un administrateur.");
        }

        user.setLastLogin(LocalDateTime.now());
        authService.save(user);

        String accessToken = authService.generateAccessToken(user);
        String refreshToken = authService.generateRefreshToken(user);

        ResponseCookie accessCookie = ResponseCookie.from("access_token", accessToken)
                .httpOnly(true).secure(secureCookies).sameSite("Strict").path("/").maxAge(900).build();

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true).secure(secureCookies).sameSite("Strict").path("/").maxAge(86400).build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString(), refreshCookie.toString())
                .body("Login OK");
    }


    @PostMapping("/refresh-token")
    public ResponseEntity<String> refreshToken(@CookieValue(name = "refresh_token", required = false) String token) {
        if (token == null || !authService.validateToken(token)) {
            return ResponseEntity.badRequest().body("Invalid refresh token");
        }

        String email = authService.extractEmailFromToken(token);
        String newAccessToken = authService.generateAccessToken(authService.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found")));

        ResponseCookie newAccessCookie = ResponseCookie.from("access_token", newAccessToken)
                .httpOnly(true).secure(secureCookies).sameSite("Strict").path("/").maxAge(900).build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, newAccessCookie.toString())
                .body("Token refreshed");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        ResponseCookie expiredAccess = ResponseCookie.from("access_token", "")
                .httpOnly(true).secure(secureCookies).sameSite("Strict").path("/").maxAge(0).build();

        ResponseCookie expiredRefresh = ResponseCookie.from("refresh_token", "")
                .httpOnly(true).secure(secureCookies).sameSite("Strict").path("/").maxAge(0).build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, expiredAccess.toString(), expiredRefresh.toString())
                .body("Logout OK");
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> me(
            @CookieValue(name = "access_token", required = false) String token
    ) {
        if (token == null || !authService.validateToken(token)) {
            return ResponseEntity.status(401).build();
        }

        String email = authService.extractEmailFromToken(token);
        var optionalUser = authService.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(404).build();
        }

        User user = optionalUser.get();

        // Récupération de tous les noms de rôles
        List<String> roleNames = user.getRoles()
                .stream()
                .map(Role::getName)
                .toList();

        UserProfileResponse response = new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getStatus().name(),
                user.getCreatedAt(),
                user.getLastLogin(),
                roleNames
        );

        return ResponseEntity.ok(response);
    }


    @PutMapping("/me")
    public ResponseEntity<?> updateProfile(
            @CookieValue(name = "access_token", required = false) String token,
            @RequestBody @Valid UpdateProfileRequest req
    ) {
        if (token == null || !authService.validateToken(token)) {
            return ResponseEntity.status(401).build();
        }

        String email = authService.extractEmailFromToken(token);
        User user = authService.findByEmail(email).orElseThrow();

        user.setFirstName(req.firstName());
        user.setLastName(req.lastName());

        authService.save(user);

        return ResponseEntity.ok("Profil mis à jour");
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @CookieValue(name = "access_token", required = false) String token,
            @RequestBody @Valid ChangePasswordRequest req
    ) {
        if (token == null || !authService.validateToken(token)) {
            return ResponseEntity.status(401).build();
        }

        String email = authService.extractEmailFromToken(token);
        User user = authService.findByEmail(email).orElseThrow();

        if (!authService.checkPassword(user, req.oldPassword())) {
            return ResponseEntity.status(403).body("Ancien mot de passe incorrect");
        }

        if (!req.newPassword().equals(req.confirmPassword())) {
            return ResponseEntity.badRequest().body("Les mots de passe ne correspondent pas");
        }

        user.setPassword(authService.encodePassword(req.newPassword()));
        authService.save(user);

        return ResponseEntity.ok("Mot de passe mis à jour avec succès");
    }





}
