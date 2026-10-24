package com.ishaworld.media.dto;

import com.ishaworld.common.enums.EntityType;

public record MediaResponse(
        Long id,
        EntityType entityType,
        Long entityId,
        String url,
        String title,
        boolean primary
) {
}

