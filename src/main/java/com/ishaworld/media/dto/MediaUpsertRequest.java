package com.ishaworld.media.dto;

import com.ishaworld.common.enums.EntityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MediaUpsertRequest(
        @NotNull EntityType entityType,
        @NotNull Long entityId,
        @NotBlank String url,
        String title,
        boolean primary
) {
}

