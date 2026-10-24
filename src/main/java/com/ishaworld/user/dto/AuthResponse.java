package com.ishaworld.user.dto;

public record AuthResponse(
        String token,
        UserResponse user
) {
}
