package org.payetonkawa.auth.auth_service.repository;

import org.payetonkawa.auth.auth_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByRoles_Name(String roleName);
}
