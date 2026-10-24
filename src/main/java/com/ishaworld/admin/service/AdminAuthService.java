package com.ishaworld.admin.service;

import com.ishaworld.admin.dto.AdminLoginRequest;
import com.ishaworld.admin.dto.AdminLoginResponse;
import com.ishaworld.admin.entity.AdminUser;
import com.ishaworld.admin.repository.AdminUserRepository;
import com.ishaworld.exception.UnauthorizedException;
import com.ishaworld.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthService {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AdminAuthService(
            AdminUserRepository adminUserRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AdminLoginResponse login(AdminLoginRequest request) {
        AdminUser admin = adminUserRepository.findByEmailIgnoreCase(request.email())
                .filter(AdminUser::isActive)
                .orElseThrow(() -> new UnauthorizedException("Invalid admin credentials"));

        if (!passwordEncoder.matches(request.password(), admin.getPasswordHash())) {
            throw new UnauthorizedException("Invalid admin credentials");
        }

        return new AdminLoginResponse(jwtService.generate(admin), admin.getEmail(), admin.getRole());
    }
}

