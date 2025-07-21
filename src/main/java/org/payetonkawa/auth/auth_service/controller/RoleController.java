package org.payetonkawa.auth.auth_service.controller;

import lombok.RequiredArgsConstructor;
import org.payetonkawa.auth.auth_service.model.Role;
import org.payetonkawa.auth.auth_service.service.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    public ResponseEntity<Role> createRole(@RequestBody String roleName) {
        Role createdRole = roleService.createRole(roleName);
        return ResponseEntity.status(201).body(createdRole);
    }

    @GetMapping
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Role> getRoleById(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.getRoleById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Role> updateRole(@PathVariable Long id, @RequestBody String newName) {
        return ResponseEntity.ok(roleService.updateRole(id, newName));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }
}
