package com.ishaworld.social.dto;

import com.ishaworld.common.enums.EntityType;
import com.ishaworld.common.enums.SocialPlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SocialLinkUpsertRequest(
        @NotNull EntityType entityType,
        @NotNull Long entityId,
        @NotNull SocialPlatform platform,
        @NotBlank String url
) {
}

