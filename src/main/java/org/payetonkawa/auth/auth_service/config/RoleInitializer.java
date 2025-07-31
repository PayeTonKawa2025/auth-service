package org.payetonkawa.auth.auth_service.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.payetonkawa.auth.auth_service.model.Role;
import org.payetonkawa.auth.auth_service.repository.RoleRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleInitializer {

    private final RoleRepository roleRepository;

    @PostConstruct
    public void initRoles() {
        createRoleIfNotExists("USER");
        createRoleIfNotExists("ADMIN");
    }

    private void createRoleIfNotExists(String roleName) {
        roleRepository.findByName(roleName).orElseGet(() -> {
            Role role = new Role();
            role.setName(roleName);
            return roleRepository.save(role);
        });
    }
}
