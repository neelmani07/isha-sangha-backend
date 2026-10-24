package com.ishaworld.meditator.dto;

import java.util.List;

public record MeditatorResponse(
        Long id,
        String displayName,
        Long cityId,
        String cityName,
        String citySlug,
        double lat,
        double lng,
        String about,
        List<String> practices,
        String languages,
        boolean openToConnect,
        boolean openToVolunteer,
        boolean visible
) {
}
