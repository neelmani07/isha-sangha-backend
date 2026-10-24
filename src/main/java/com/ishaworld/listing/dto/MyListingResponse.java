package com.ishaworld.listing.dto;

import com.ishaworld.common.enums.ListingStatus;
import java.time.Instant;

/** A listing as its submitter sees it, including its review status. */
public record MyListingResponse(
        String kind,
        Long id,
        String name,
        String slug,
        String cityName,
        ListingStatus status,
        String rejectionReason,
        Instant submittedAt
) {
}
