package com.ishaworld.initiative.dto;

import com.ishaworld.common.enums.InitiativeKind;
import java.util.List;

public record InitiativeResponse(
        Long id,
        String slug,
        String title,
        InitiativeKind kind,
        String summary,
        String description,
        Long cityId,
        String cityName,
        String citySlug,
        Double lat,
        Double lng,
        Integer startedYear,
        String startedByName,
        List<String> needs,
        String needsNote,
        String contactWhatsapp,
        String contactEmail,
        String contactWebsite,
        String photoUrl,
        long supporters,
        boolean supportedByMe
) {
}
