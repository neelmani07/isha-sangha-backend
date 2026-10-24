package com.ishaworld.city.dto;

public record CommunityStatsResponse(
        int meditatorCount,
        int volunteerCount,
        int teacherCount,
        int yantraCount,
        int sannidhiCount,
        int centerCount,
        int templeCount,
        int shopCount
) {
}

