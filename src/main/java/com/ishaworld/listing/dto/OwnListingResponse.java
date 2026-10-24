package com.ishaworld.listing.dto;

import com.ishaworld.center.dto.CenterResponse;
import com.ishaworld.common.enums.ListingStatus;
import com.ishaworld.initiative.dto.InitiativeResponse;
import com.ishaworld.resourcelink.dto.ResourceLinkResponse;
import com.ishaworld.teacher.dto.TeacherResponse;

/** A submitter's own listing with everything needed to edit and resubmit it. */
public record OwnListingResponse(
        ListingStatus status,
        String rejectionReason,
        String proofKey,
        TeacherResponse teacher,
        CenterResponse center,
        InitiativeResponse initiative,
        ResourceLinkResponse resource
) {
}
