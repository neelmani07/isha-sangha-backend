package com.ishaworld.admin.dto;

import com.ishaworld.common.enums.AdminRole;

public record AdminLoginResponse(
        String token,
        String email,
        AdminRole role
) {
}

