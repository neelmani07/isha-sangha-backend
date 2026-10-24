package com.ishaworld.listing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectListingRequest(
        /** Shown to the submitter so they can fix and resubmit. */
        @NotBlank @Size(max = 500) String reason
) {
}
