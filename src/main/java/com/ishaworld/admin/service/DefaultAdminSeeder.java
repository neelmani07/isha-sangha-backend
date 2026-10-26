package com.ishaworld.admin.service;

import com.ishaworld.admin.entity.AdminUser;
import com.ishaworld.admin.repository.AdminUserRepository;
import com.ishaworld.common.enums.AdminRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates the first super admin from ADMIN_DEFAULT_EMAIL / ADMIN_DEFAULT_PASSWORD if that account does not exist yet.
 * The password is only used on first creation; change it afterwards and the variable can be removed.
 */
@Component
public class DefaultAdminSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DefaultAdminSeeder.class);

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final String defaultEmail;
    private final String defaultPassword;

    public DefaultAdminSeeder(
            AdminUserRepository adminUserRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.default-email:admin@ishaworld.local}") String defaultEmail,
            @Value("${app.admin.default-password:}") String defaultPassword
    ) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.defaultEmail = defaultEmail;
        this.defaultPassword = defaultPassword;
    }

    @Override
    public void run(String... args) {
        if (defaultPassword == null || defaultPassword.isBlank()) {
            if (adminUserRepository.count() == 0) {
                log.warn("No admin account exists. Set ADMIN_DEFAULT_EMAIL and ADMIN_DEFAULT_PASSWORD once to create the first one.");
            }
            return;
        }
        adminUserRepository.findByEmailIgnoreCase(defaultEmail).orElseGet(() -> {
            AdminUser admin = new AdminUser();
            admin.setEmail(defaultEmail);
            admin.setPasswordHash(passwordEncoder.encode(defaultPassword));
            admin.setRole(AdminRole.SUPER_ADMIN);
            admin.setActive(true);
            return adminUserRepository.save(admin);
        });
    }
}

