package com.ishaworld.story.dto;

import com.ishaworld.common.enums.StoryCategory;
import java.time.Instant;

public record StoryResponse(
        Long id,
        String slug,
        String title,
        StoryCategory category,
        String excerpt,
        String body,
        String authorName,
        /** Where it happened: "city" or "center", with its slug and name; null when not linked. */
        String placeKind,
        String placeSlug,
        String placeName,
        Long cityId,
        String photoUrl,
        boolean featured,
        Instant publishedAt,
        int readMinutes,
        long likes,
        boolean likedByMe
) {
}
