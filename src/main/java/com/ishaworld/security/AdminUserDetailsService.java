package com.ishaworld.security;

import com.ishaworld.admin.repository.AdminUserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AdminUserDetailsService implements UserDetailsService {

    private final AdminUserRepository adminUserRepository;

    public AdminUserDetailsService(AdminUserRepository adminUserRepository) {
        this.adminUserRepository = adminUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var admin = adminUserRepository.findByEmailIgnoreCase(username)
                .filter(user -> user.isActive())
                .orElseThrow(() -> new UsernameNotFoundException("Admin user not found"));

        return User.withUsername(admin.getEmail())
                .password(admin.getPasswordHash())
                .roles(admin.getRole().name())
                .build();
    }
}

