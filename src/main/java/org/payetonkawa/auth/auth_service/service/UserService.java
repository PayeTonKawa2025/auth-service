package org.payetonkawa.auth.auth_service.service;

import lombok.RequiredArgsConstructor;
import org.payetonkawa.auth.auth_service.model.User;
import org.payetonkawa.auth.auth_service.model.Role;
import org.payetonkawa.auth.auth_service.repository.UserRepository;
import org.payetonkawa.auth.auth_service.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

    public Optional<User> update(Long id, User updatedUser) {
        return repo.findById(id).map(existing -> {
            existing.setFirstName(updatedUser.getFirstName());
            existing.setLastName(updatedUser.getLastName());
            existing.setEmail(updatedUser.getEmail());
            return repo.save(existing);
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

    public void updateUserRoles(Long userId, List<String> roleNames) {
        User user = repo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Set<Role> roles = roleNames.stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new RuntimeException("Role " + roleName + " not found")))
                .collect(Collectors.toSet());

        user.setRoles(roles);
        repo.save(user);
    }

    public Set<String> getUserRoles(Long userId) {
        User user = repo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
    }

}
