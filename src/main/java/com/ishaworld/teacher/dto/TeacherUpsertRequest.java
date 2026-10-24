package com.ishaworld.teacher.dto;

import com.ishaworld.common.enums.TeachingMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record TeacherUpsertRequest(
        @NotNull Long cityId,
        String slug,
        @NotBlank String name,
        String bio,
        String photoUrl,
        BigDecimal latitude,
        BigDecimal longitude,
        String phone,
        String email,
        String website,
        Boolean active,
        List<Long> programIds,
        TeachingMode teachingMode,
        String languages,
        Integer yearsTeaching,
        Boolean verified
) {
}

