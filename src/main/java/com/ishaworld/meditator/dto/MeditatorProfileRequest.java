package com.ishaworld.meditator.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record MeditatorProfileRequest(
        /** Full name as typed; only first name + last initial is ever shown. */
        @NotBlank @Size(max = 80) String name,
        @NotNull Long cityId,
        @Size(max = 500) String about,
        @Size(max = 12) List<@Size(max = 40) String> practices,
        @Size(max = 255) String languages,
        boolean openToConnect,
        boolean openToVolunteer,
        /** Defaults to true: members are visible unless they choose to hide. */
        Boolean visible
) {
}
