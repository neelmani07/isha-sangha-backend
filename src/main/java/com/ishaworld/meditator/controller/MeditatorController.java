package com.ishaworld.meditator.controller;

import com.ishaworld.common.api.ApiResponse;
import com.ishaworld.common.api.PageResponse;
import com.ishaworld.meditator.dto.MeditatorResponse;
import com.ishaworld.meditator.service.MeditatorService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/meditators")
public class MeditatorController {

    private final MeditatorService meditatorService;

    public MeditatorController(MeditatorService meditatorService) {
        this.meditatorService = meditatorService;
    }

    /** Publicly visible meditators, optionally for one city. Locations are approximate by design. */
    @GetMapping
    ApiResponse<PageResponse<MeditatorResponse>> list(
            @RequestParam(required = false) Long cityId,
            @PageableDefault(size = 50) Pageable pageable
    ) {
        return ApiResponse.success(meditatorService.findVisible(cityId, pageable));
    }
}
