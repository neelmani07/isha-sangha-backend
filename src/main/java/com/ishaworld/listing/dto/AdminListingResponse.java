package com.ishaworld.listing.dto;

import com.ishaworld.center.dto.CenterResponse;
import com.ishaworld.city.dto.PickedCityResponse;
import com.ishaworld.common.enums.ListingStatus;
import com.ishaworld.initiative.dto.InitiativeResponse;
import com.ishaworld.resourcelink.dto.ResourceLinkResponse;
import com.ishaworld.shop.dto.ShopListingResponse;
import com.ishaworld.story.dto.StoryResponse;
import com.ishaworld.teacher.dto.TeacherResponse;
import java.time.Instant;

/** A submitted listing as the review queue shows it: the listing itself plus who sent it and their proof. */
public record AdminListingResponse(
        String kind,
        Long id,
        ListingStatus status,
        String submittedByEmail,
        String proofKey,
        Instant submittedAt,
        String rejectionReason,
        TeacherResponse teacher,
        CenterResponse center,
        InitiativeResponse initiative,
        ResourceLinkResponse resource,
        PickedCityResponse city,
        StoryResponse story,
        ShopListingResponse product
) {
}
