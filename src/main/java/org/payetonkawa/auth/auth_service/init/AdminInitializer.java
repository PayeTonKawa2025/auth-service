package org.payetonkawa.auth.auth_service.init;

import lombok.RequiredArgsConstructor;
import org.payetonkawa.auth.auth_service.model.Role;
import org.payetonkawa.auth.auth_service.model.User;
import org.payetonkawa.auth.auth_service.repository.RoleRepository;
import org.payetonkawa.auth.auth_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import org.slf4j.Logger;

@Configuration
@RequiredArgsConstructor
public class AdminInitializer {

    private final RoleRepository roleRepo;
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(AdminInitializer.class);

    @Value("${admin.email:}")
    private String adminEmail;

    @Value("${admin.password:}")
    private String adminPassword;

    @Bean
    public Runnable initAdmin() {
        return () -> {
            if (adminEmail.isBlank() || adminPassword.isBlank()) {
                logger.warn("Admin creation skipped: missing ADMIN_EMAIL or ADMIN_PASSWORD.");
                return;
            }

            Role adminRole = roleRepo.findByName("ADMIN")
                    .orElseGet(() -> roleRepo.save(new Role("ADMIN")));


            Optional<User> existingAdmin = userRepo.findByEmail(adminEmail);

            if (existingAdmin.isEmpty()) {
                User admin = new User();
                admin.setEmail(adminEmail);
                admin.setPassword(passwordEncoder.encode(adminPassword));
                admin.setFirstName("Admin");
                admin.setLastName("Account");
                admin.getRoles().add(adminRole);
                userRepo.save(admin);
                logger.info("Admin user created: {}", adminEmail);
            } else {
                logger.info("Admin user already exists: {}", adminEmail);
            }
        };
    }
}
