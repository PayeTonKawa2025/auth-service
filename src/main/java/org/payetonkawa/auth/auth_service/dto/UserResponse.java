package org.payetonkawa.auth.auth_service.dto;

import java.time.LocalDateTime;
import java.util.Set;

public record UserResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        String status,             // "ACTIVE" ou "INACTIVE"
        LocalDateTime createdAt,
        LocalDateTime lastLogin,
        Set<RoleResponse> roles
) {}
