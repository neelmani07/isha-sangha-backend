package com.ishaworld.resourcelink.dto;

import com.ishaworld.common.enums.ResourceCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ResourceSubmissionRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 1000) String url,
        @Size(max = 500) String description,
        @NotNull ResourceCategory category,
        Long cityId,
        @Size(max = 60) String language,
        boolean membersOnly
) {
}
