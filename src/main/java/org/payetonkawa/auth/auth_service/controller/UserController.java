package org.payetonkawa.auth.auth_service.controller;

import lombok.RequiredArgsConstructor;
import org.payetonkawa.auth.auth_service.dto.RoleResponse;
import org.payetonkawa.auth.auth_service.dto.UpdateUserRequest;
import org.payetonkawa.auth.auth_service.dto.UserResponse;
import org.payetonkawa.auth.auth_service.model.User;
import org.payetonkawa.auth.auth_service.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/auth/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService service;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getUsers() {
        List<User> users = service.findAll();

        List<UserResponse> userResponses = users.stream().map(user -> {
            Set<RoleResponse> roleResponses = user.getRoles().stream()
                    .map(role -> new RoleResponse(role.getId(), role.getName()))
                    .collect(Collectors.toSet());

            return new UserResponse(
                    user.getId(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getStatus().name(),
                    user.getCreatedAt(),
                    user.getLastLogin(),
                    roleResponses
            );
        }).toList();

        return ResponseEntity.ok(userResponses);
    }


    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(user -> {
                    Set<RoleResponse> roles = user.getRoles().stream()
                            .map(r -> new RoleResponse(r.getId(), r.getName()))
                            .collect(Collectors.toSet());

                    return ResponseEntity.ok(new UserResponse(
                            user.getId(),
                            user.getEmail(),
                            user.getFirstName(),
                            user.getLastName(),
                            user.getStatus().name(),
                            user.getCreatedAt(),
                            user.getLastLogin(),
                            roles
                    ));
                })
                .orElse(ResponseEntity.notFound().build());
    }


    @PutMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable Long id, @RequestBody UpdateUserRequest dto) {
        return service.update(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
