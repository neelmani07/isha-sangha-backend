package com.ishaworld.social.dto;

import com.ishaworld.common.enums.EntityType;
import com.ishaworld.common.enums.SocialPlatform;

public record SocialLinkResponse(
        Long id,
        EntityType entityType,
        Long entityId,
        SocialPlatform platform,
        String url
) {
}

