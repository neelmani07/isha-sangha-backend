package com.ishaworld.country.dto;

import java.math.BigDecimal;

public record CountryResponse(
        Long id,
        String name,
        String isoCode,
        BigDecimal latitude,
        BigDecimal longitude,
        long cities,
        long teachers,
        long centers
) {
}

