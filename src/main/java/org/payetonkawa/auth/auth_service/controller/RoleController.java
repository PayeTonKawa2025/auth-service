package org.payetonkawa.auth.auth_service.controller;

import lombok.RequiredArgsConstructor;
import org.payetonkawa.auth.auth_service.dto.RoleCreateRequest;
import org.payetonkawa.auth.auth_service.dto.RoleUpdateRequest;
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
    public ResponseEntity<Role> createRole(@RequestBody RoleCreateRequest dto) {
        return ResponseEntity.status(201).body(roleService.createRole(dto.name()));
    }

    @GetMapping
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Role> updateRole(@PathVariable Long id, @RequestBody RoleUpdateRequest dto) {
        return ResponseEntity.ok(roleService.updateRole(id, dto.name()));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }
}
