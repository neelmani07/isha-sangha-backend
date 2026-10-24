package com.ishaworld.city.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** A city a member found through the worldwide map search (OpenStreetMap). */
public record MemberCityRequest(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 150) String stateName,
        @NotBlank @Size(max = 150) String countryName,
        @NotBlank @Pattern(regexp = "^[A-Za-z]{2}$", message = "must be a two-letter country code") String countryCode,
        @NotNull @DecimalMin("-90") @DecimalMax("90") Double latitude,
        @NotNull @DecimalMin("-180") @DecimalMax("180") Double longitude
) {
}
