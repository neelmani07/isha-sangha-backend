package com.ishaworld.story.dto;

import com.ishaworld.common.enums.StoryCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StorySubmissionRequest(
        @NotBlank @Size(max = 200) String title,
        @NotNull StoryCategory category,
        @NotBlank @Size(min = 80, max = 12000, message = "should be between 80 and 12,000 characters") String body,
        /** Name shown with the story; defaults to first name + initial. */
        @Size(max = 120) String authorName,
        Long cityId,
        Long centerId,
        String photoKey
) {
}
