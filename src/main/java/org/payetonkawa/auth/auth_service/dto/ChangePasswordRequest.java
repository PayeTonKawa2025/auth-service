package org.payetonkawa.auth.auth_service.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
        @NotBlank String oldPassword,
        @NotBlank String newPassword,
        @NotBlank String confirmPassword
) {}
