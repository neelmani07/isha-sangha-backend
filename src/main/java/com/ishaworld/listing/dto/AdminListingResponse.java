package com.ishaworld.listing.dto;

import com.ishaworld.center.dto.CenterResponse;
import com.ishaworld.common.enums.ListingStatus;
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
        CenterResponse center
) {
}
