package org.payetonkawa.auth.auth_service.controller;

import lombok.RequiredArgsConstructor;
import org.payetonkawa.auth.auth_service.model.User;
import org.payetonkawa.auth.auth_service.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("api/auth/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @GetMapping
    public ResponseEntity<List<User>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @PostMapping

    public ResponseEntity<User> create(@RequestBody User user) {
        if (user.getEmail() == null || user.getPassword() == null) {
            return ResponseEntity.badRequest().build();
        }
        User createdUser = service.create(user);
        return ResponseEntity.status(201).body(createdUser);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable Long id, @RequestBody User updatedUser) {
        return service.update(id, updatedUser)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @PutMapping("/{userId}/roles")
    public ResponseEntity<String> updateUserRoles(
            @PathVariable Long userId,
            @RequestBody List<String> roleNames) {
        try {
            service.updateUserRoles(userId, roleNames);
            return ResponseEntity.ok("User roles updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{userId}/roles")
    public ResponseEntity<Set<String>> getUserRoles(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getUserRoles(userId));
    }


}
