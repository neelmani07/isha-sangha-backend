package com.ishaworld.listing.dto;

import com.ishaworld.common.enums.TeachingMode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public record TeacherSubmissionRequest(
        @NotBlank @Size(max = 250) String name,
        @NotNull Long cityId,
        @Size(max = 2000) String bio,
        List<Long> programIds,
        TeachingMode teachingMode,
        @Size(max = 255) String languages,
        @Min(0) @Max(80) Integer yearsTeaching,
        @Size(max = 50) String phone,
        @Email @Size(max = 255) String email,
        @Size(max = 255) String website,
        BigDecimal latitude,
        BigDecimal longitude,
        /** Key returned by the proof upload; required for teachers. */
        @NotBlank String proofKey
) {
}
