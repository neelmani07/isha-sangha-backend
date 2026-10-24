package com.ishaworld.city.dto;

import java.math.BigDecimal;

public record CityResponse(
        Long id,
        Long countryId,
        String countryName,
        Long stateId,
        String stateName,
        String slug,
        String name,
        BigDecimal latitude,
        BigDecimal longitude,
        String description,
        String heroImageUrl,
        boolean active,
        CommunityStatsResponse stats
) {
}

