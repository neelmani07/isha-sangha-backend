package com.ishaworld.state.dto;

import java.math.BigDecimal;

public record StateResponse(
        Long id,
        Long countryId,
        String countryName,
        String name,
        BigDecimal latitude,
        BigDecimal longitude,
        long cities
) {
}

