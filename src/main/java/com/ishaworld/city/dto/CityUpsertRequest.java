package com.ishaworld.city.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CityUpsertRequest(
        @NotNull Long countryId,
        @NotNull Long stateId,
        String slug,
        @NotBlank String name,
        BigDecimal latitude,
        BigDecimal longitude,
        String description,
        String heroImageUrl,
        boolean active,
        CommunityStatsUpsertRequest stats
) {
}

