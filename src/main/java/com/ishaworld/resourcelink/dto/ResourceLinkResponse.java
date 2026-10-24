package com.ishaworld.resourcelink.dto;

import com.ishaworld.common.enums.ResourceCategory;
import java.time.Instant;

public record ResourceLinkResponse(
        Long id,
        String title,
        /** Null for members-only links when the viewer is not signed in. */
        String url,
        String host,
        String description,
        ResourceCategory category,
        boolean membersOnly,
        Long cityId,
        String cityName,
        String language,
        long helpful,
        boolean helpfulByMe,
        Instant sharedAt
) {
}
