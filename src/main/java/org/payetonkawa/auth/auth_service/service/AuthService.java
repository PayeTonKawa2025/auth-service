package org.payetonkawa.auth.auth_service.service;

import lombok.RequiredArgsConstructor;
import org.payetonkawa.auth.auth_service.model.User;
import org.payetonkawa.auth.auth_service.repository.UserRepository;
import org.payetonkawa.auth.auth_service.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.payetonkawa.auth.auth_service.model.Role;
import org.payetonkawa.auth.auth_service.repository.RoleRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final RoleRepository roleRepository;

    public boolean checkPassword(User user, String rawPassword) {
        return encoder.matches(rawPassword, user.getPassword());
    }

    public String generateAccessToken(User user) {
        return jwtService.generateAccessToken(user);
    }
    public String generateRefreshToken(User user) {
        return jwtService.generateRefreshToken(user);
    }

    public Optional<User> findByEmail(String email) {
        return repo.findByEmail(email);
    }

    public void register(User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        repo.save(user);
    }

    public Role getDefaultUserRole() {
        return roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Default role USER not found in database"));
    }

    public String extractEmailFromToken(String token) {
        return jwtService.getEmailFromToken(token);
    }
    public boolean validateToken(String token) {
        return jwtService.validateToken(token);
    }
}
