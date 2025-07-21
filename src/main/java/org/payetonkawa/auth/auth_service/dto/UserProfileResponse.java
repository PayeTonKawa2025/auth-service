package org.payetonkawa.auth.auth_service.dto;

public record UserProfileResponse(
        Long id,
        String email,
        String firstname,
        String lastname,
        String role
) {}
