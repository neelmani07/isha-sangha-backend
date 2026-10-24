package com.ishaworld.nearby.dto;

public record NearbyResultResponse(
        String type,
        Long id,
        String slug,
        String name,
        double lat,
        double lng,
        double distanceKm,
        String subtitle,
        String citySlug
) {
}
