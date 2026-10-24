package com.ishaworld.teacher.dto;

import com.ishaworld.common.enums.TeachingMode;
import com.ishaworld.program.dto.ProgramResponse;
import java.math.BigDecimal;
import java.util.List;

public record TeacherResponse(
        Long id,
        Long cityId,
        String cityName,
        String slug,
        String name,
        String bio,
        String photoUrl,
        BigDecimal latitude,
        BigDecimal longitude,
        String phone,
        String email,
        String website,
        boolean active,
        List<ProgramResponse> programs,
        TeachingMode teachingMode,
        String languages,
        Integer yearsTeaching,
        boolean verified
) {
}

