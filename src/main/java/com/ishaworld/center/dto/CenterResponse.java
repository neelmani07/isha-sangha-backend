package com.ishaworld.center.dto;

import com.ishaworld.common.enums.CenterType;
import java.math.BigDecimal;

public record CenterResponse(
        Long id,
        Long cityId,
        String cityName,
        String slug,
        String name,
        CenterType type,
        String description,
        String address,
        BigDecimal latitude,
        BigDecimal longitude,
        String phone,
        String email,
        String website,
        boolean active,
        String openingHours,
        String photoUrl,
        String languages,
        boolean verified
) {
}

