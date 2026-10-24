package com.ishaworld.country.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record CountryUpsertRequest(
        @NotBlank String name,
        @NotBlank String isoCode,
        BigDecimal latitude,
        BigDecimal longitude
) {
}

