package org.payetonkawa.auth.auth_service.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.payetonkawa.auth.auth_service.dto.UpdateUserRequest;
import org.payetonkawa.auth.auth_service.model.Status;
import org.payetonkawa.auth.auth_service.model.User;
import org.payetonkawa.auth.auth_service.model.Role;
import org.payetonkawa.auth.auth_service.repository.UserRepository;
import org.payetonkawa.auth.auth_service.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repo;
    private final RoleRepository roleRepository;

    public List<User> findAll() {
        return repo.findAll();
    }

    public Optional<User> findById(Long id) {
        return repo.findById(id);
    }

    @Transactional
    public Optional<User> update(Long id, UpdateUserRequest dto) {
        return repo.findById(id).map(user -> {
            System.out.println("🟡 Mise à jour de l'utilisateur ID " + id);
            System.out.println("Payload reçu: " + dto);

            user.setFirstName(dto.firstName());
            user.setLastName(dto.lastName());
            user.setEmail(dto.email());

            if (dto.status() != null) {
                System.out.println("➡️ Status: " + dto.status());
                user.setStatus(Status.valueOf(dto.status().toUpperCase()));
            }

            if (dto.role() != null) {
                System.out.println("➡️ Rôle: " + dto.role());
                Role role = roleRepository.findByName(dto.role().toUpperCase())
                        .orElseThrow(() -> new RuntimeException("Role not found: " + dto.role()));
                user.getRoles().clear();
                user.getRoles().add(role);
            }

            return repo.save(user);
        });
    }



    public boolean delete(Long id) {
        if (!repo.existsById(id)) return false;
        repo.deleteById(id);
        return true;
    }

    public User create(User user) {
        if (user.getEmail() == null || user.getPassword() == null) {
            throw new IllegalArgumentException("Email and password must not be null");
        }
        return repo.save(user);
    }

}
