package com.ishaworld.admin.service;

import com.ishaworld.admin.entity.AdminUser;
import com.ishaworld.admin.repository.AdminUserRepository;
import com.ishaworld.common.enums.AdminRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DefaultAdminSeeder implements CommandLineRunner {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final String defaultEmail;
    private final String defaultPassword;

    public DefaultAdminSeeder(
            AdminUserRepository adminUserRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.default-email:admin@ishaworld.local}") String defaultEmail,
            @Value("${app.admin.default-password:admin123}") String defaultPassword
    ) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.defaultEmail = defaultEmail;
        this.defaultPassword = defaultPassword;
    }

    @Override
    public void run(String... args) {
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

