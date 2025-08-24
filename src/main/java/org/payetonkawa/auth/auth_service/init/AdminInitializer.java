package org.payetonkawa.auth.auth_service.init;

import lombok.RequiredArgsConstructor;
import org.payetonkawa.auth.auth_service.model.Role;
import org.payetonkawa.auth.auth_service.model.Status;
import org.payetonkawa.auth.auth_service.model.User;
import org.payetonkawa.auth.auth_service.repository.RoleRepository;
import org.payetonkawa.auth.auth_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminInitializer.class);

    private final RoleRepository roleRepo;
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.email:}")
    private String adminEmail;

    @Value("${admin.password:}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        logger.info("🔧 Initialisation de l'utilisateur admin...");

        if (adminEmail.isBlank()) {
            logger.warn("❌ Admin creation skipped: admin.email is missing.");
            return;
        }

        try {
            // Génération du mot de passe temporaire si non fourni
            boolean isTempPassword = false;
            if (adminPassword == null || adminPassword.isBlank()) {
                adminPassword = generateRandomPassword(12);
                isTempPassword = true;
            }

            // Récupération ou création du rôle
            logger.debug("🔍 Recherche du rôle 'ADMIN'...");
            Role adminRole = roleRepo.findByName("ADMIN")
                    .orElseGet(() -> {
                        logger.info("🆕 Rôle 'ADMIN' introuvable, création...");
                        return roleRepo.save(new Role("ADMIN"));
                    });

            // Vérification de l'utilisateur admin
            logger.debug("🔍 Recherche de l'utilisateur avec l'email '{}'", adminEmail);
            Optional<User> existingAdmin = userRepo.findByEmail(adminEmail);

            if (existingAdmin.isEmpty()) {
                logger.info("✅ Aucun utilisateur admin trouvé. Création en cours...");

                User admin = new User();
                admin.setEmail(adminEmail);
                admin.setPassword(passwordEncoder.encode(adminPassword));
                admin.setFirstName("Admin");
                admin.setLastName("Account");
                admin.getRoles().add(adminRole);
                admin.setStatus(Status.ACTIVE); // Assurez-vous que le statut est actif
                admin.setCreatedAt(LocalDateTime.now());
                admin.setLastLogin(LocalDateTime.now()); // Initialiser lastLogin

                userRepo.save(admin);

                logger.info("✅ Admin user created with email: {}", adminEmail);
                if (isTempPassword) {
                    logger.warn("🔐 Mot de passe temporaire généré pour l'admin : {}", adminPassword);
                } else {
                    logger.info("ℹ️ Un mot de passe personnalisé a été utilisé (non affiché).");
                }
                logger.info("⚠️ Pensez à changer le mot de passe après la première connexion.");
            } else {
                logger.info("ℹ️ L'utilisateur admin existe déjà : {}", adminEmail);
            }

        } catch (Exception e) {
            logger.error("❌ Erreur lors de l'initialisation de l'admin : {}", e.getMessage(), e);
        }
    }

    private String generateRandomPassword(int length) {
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length());
            password.append(chars.charAt(index));
        }

        return password.toString();
    }
}
