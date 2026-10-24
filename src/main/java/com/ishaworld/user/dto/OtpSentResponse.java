package com.ishaworld.user.dto;

public record OtpSentResponse(
        String email,
        long expiresInSeconds
) {
}
