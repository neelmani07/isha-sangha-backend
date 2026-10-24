package com.ishaworld.city.dto;

import com.ishaworld.common.enums.ListingStatus;

/** The city a member can now use in their profile or listing; PENDING until an admin approves it. */
public record PickedCityResponse(
        Long id,
        String name,
        String stateName,
        String countryName,
        double lat,
        double lng,
        ListingStatus status
) {
}
