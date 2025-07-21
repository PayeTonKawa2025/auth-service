package org.payetonkawa.auth.auth_service.service;

import lombok.RequiredArgsConstructor;
import org.payetonkawa.auth.auth_service.model.Role;
import org.payetonkawa.auth.auth_service.repository.RoleRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public Role createRole(String name) {
        if (roleRepository.findByName(name).isPresent()) {
            throw new RuntimeException("Role already exists");
        }
        Role role = new Role();
        role.setName(name);
        return roleRepository.save(role);
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Role getRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));
    }

    public Role updateRole(Long id, String newName) {
        Role role = getRoleById(id);
        role.setName(newName);
        return roleRepository.save(role);
    }

    public void deleteRole(Long id) {
        if (!roleRepository.existsById(id)) {
            throw new RuntimeException("Role not found");
        }
        roleRepository.deleteById(id);
    }
}
