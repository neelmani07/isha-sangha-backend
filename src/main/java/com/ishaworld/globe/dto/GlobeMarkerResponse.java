package com.ishaworld.globe.dto;

public record GlobeMarkerResponse(
        Long id,
        String slug,
        String name,
        String type,
        double lat,
        double lng,
        Integer teacherCount,
        Integer meditatorCount
) {
}
