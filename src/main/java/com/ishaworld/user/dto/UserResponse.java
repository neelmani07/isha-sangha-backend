package com.ishaworld.user.dto;

public record UserResponse(
        Long id,
        String email,
        String name,
        String avatarUrl
) {
}
