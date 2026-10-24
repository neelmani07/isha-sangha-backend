package com.ishaworld.initiative.dto;

import com.ishaworld.common.enums.InitiativeKind;
import com.ishaworld.common.enums.InitiativeNeed;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record InitiativeSubmissionRequest(
        @NotBlank @Size(max = 200) String title,
        @NotNull InitiativeKind kind,
        @NotBlank @Size(max = 300) String summary,
        @Size(max = 5000) String description,
        @NotNull Long cityId,
        @Min(1950) @Max(2100) Integer startedYear,
        @Size(max = 120) String startedByName,
        Set<InitiativeNeed> needs,
        @Size(max = 1000) String needsNote,
        /** Digits with country code, e.g. 919876543210. */
        @Pattern(regexp = "^$|^\\+?[0-9 ]{8,20}$", message = "must be a phone number with country code") String contactWhatsapp,
        @Email @Size(max = 255) String contactEmail,
        @Size(max = 500) String contactWebsite,
        /** Key returned by the image upload; optional. */
        String photoKey
) {
}
