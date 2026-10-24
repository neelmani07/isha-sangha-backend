package com.ishaworld.state.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record StateUpsertRequest(
        @NotNull Long countryId,
        @NotBlank String name,
        BigDecimal latitude,
        BigDecimal longitude
) {
}

