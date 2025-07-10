package org.payetonkawa.auth.auth_service.service;

import lombok.RequiredArgsConstructor;
import org.payetonkawa.auth.auth_service.model.User;
import org.payetonkawa.auth.auth_service.repository.UserRepository;
import org.payetonkawa.auth.auth_service.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public boolean checkPassword(User user, String rawPassword) {
        return encoder.matches(rawPassword, user.getPassword());
    }

    public String generateAccessToken(String email) {
        return jwtService.generateAccessToken(email);
    }

    public String generateRefreshToken(String email) {
        return jwtService.generateRefreshToken(email);
    }

    public Optional<User> findByEmail(String email) {
        return repo.findByEmail(email);
    }

    public void register(User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        repo.save(user);
    }

    public String extractEmailFromToken(String token) {
        return jwtService.getEmailFromToken(token);
    }
    public boolean validateToken(String token) {
        return jwtService.validateToken(token);
    }
}
