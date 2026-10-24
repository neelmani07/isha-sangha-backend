package com.ishaworld.feedback.dto;

import com.ishaworld.common.enums.FeedbackType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FeedbackRequest(
        String name,
        String email,
        @NotNull FeedbackType type,
        @NotBlank String message
) {
}

