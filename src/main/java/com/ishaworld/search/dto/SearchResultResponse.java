package com.ishaworld.search.dto;

public record SearchResultResponse(
        String type,
        Long id,
        String slug,
        String name,
        Double lat,
        Double lng
) {
}

