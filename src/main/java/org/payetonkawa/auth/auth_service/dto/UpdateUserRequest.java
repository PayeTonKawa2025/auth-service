// dto/UpdateUserRequest.java
package org.payetonkawa.auth.auth_service.dto;

public record UpdateUserRequest(
        String firstName,
        String lastName,
        String email,
        String role,
        String status // ex: "ACTIVE" ou "INACTIVE"
) {}
