package com.ishaworld.feedback.dto;

import com.ishaworld.common.enums.FeedbackType;
import java.time.Instant;

public record FeedbackResponse(
        Long id,
        String name,
        String email,
        FeedbackType type,
        String message,
        Instant createdAt
) {
}

