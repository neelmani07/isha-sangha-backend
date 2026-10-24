package com.ishaworld.listing.dto;

import com.ishaworld.common.enums.CenterType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CenterSubmissionRequest(
        @NotBlank @Size(max = 250) String name,
        @NotNull CenterType type,
        @NotNull Long cityId,
        @Size(max = 2000) String description,
        @Size(max = 500) String address,
        BigDecimal latitude,
        BigDecimal longitude,
        @Size(max = 50) String phone,
        @Email @Size(max = 255) String email,
        @Size(max = 255) String website,
        @Size(max = 500) String openingHours,
        @Size(max = 255) String languages,
        /** Key returned by the proof upload; required for centers. */
        @NotBlank String proofKey
) {
}
