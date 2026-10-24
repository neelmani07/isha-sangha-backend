package com.ishaworld.center.controller;

import com.ishaworld.center.dto.CenterResponse;
import com.ishaworld.center.service.CenterService;
import com.ishaworld.common.api.ApiResponse;
import com.ishaworld.common.api.PageResponse;
import com.ishaworld.common.enums.CenterType;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/centers")
public class CenterController {

    private final CenterService centerService;

    public CenterController(CenterService centerService) {
        this.centerService = centerService;
    }

    @GetMapping
    ApiResponse<PageResponse<CenterResponse>> findAll(
            @RequestParam(required = false) CenterType type,
            Pageable pageable
    ) {
        return ApiResponse.success(centerService.findAll(type, null, null, pageable));
    }

    @GetMapping("/{id}")
    ApiResponse<CenterResponse> findById(@PathVariable Long id) {
        return ApiResponse.success(centerService.findById(id));
    }

    @GetMapping("/slug/{slug}")
    ApiResponse<CenterResponse> findBySlug(@PathVariable String slug) {
        return ApiResponse.success(centerService.findBySlug(slug));
    }
}

