package com.ishaworld.program.dto;

import jakarta.validation.constraints.NotBlank;

public record ProgramUpsertRequest(
        @NotBlank String name,
        String description
) {
}

