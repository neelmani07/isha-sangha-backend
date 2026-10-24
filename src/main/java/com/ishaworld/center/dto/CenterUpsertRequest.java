package com.ishaworld.center.dto;

import com.ishaworld.common.enums.CenterType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CenterUpsertRequest(
        @NotNull Long cityId,
        String slug,
        @NotBlank String name,
        @NotNull CenterType type,
        String description,
        String address,
        BigDecimal latitude,
        BigDecimal longitude,
        String phone,
        String email,
        String website,
        Boolean active,
        String openingHours,
        String photoUrl,
        String languages,
        Boolean verified
) {
}

