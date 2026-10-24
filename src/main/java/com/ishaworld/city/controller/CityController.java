package com.ishaworld.city.controller;

import com.ishaworld.center.dto.CenterResponse;
import com.ishaworld.center.service.CenterService;
import com.ishaworld.city.dto.CityResponse;
import com.ishaworld.city.dto.CommunityStatsResponse;
import com.ishaworld.city.service.CityService;
import com.ishaworld.common.api.ApiResponse;
import com.ishaworld.common.api.PageResponse;
import com.ishaworld.common.enums.CenterType;
import com.ishaworld.teacher.dto.TeacherResponse;
import com.ishaworld.teacher.service.TeacherService;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cities")
public class CityController {

    private final CityService cityService;
    private final TeacherService teacherService;
    private final CenterService centerService;

    public CityController(CityService cityService, TeacherService teacherService, CenterService centerService) {
        this.cityService = cityService;
        this.teacherService = teacherService;
        this.centerService = centerService;
    }

    @GetMapping
    ApiResponse<PageResponse<CityResponse>> findAll(
            @RequestParam(required = false) Long countryId,
            @RequestParam(required = false) Long stateId,
            Pageable pageable
    ) {
        return ApiResponse.success(cityService.findAll(countryId, stateId, pageable));
    }

    @GetMapping("/{id}")
    ApiResponse<CityResponse> findById(@PathVariable Long id) {
        return ApiResponse.success(cityService.findById(id));
    }

    @GetMapping("/slug/{slug}")
    ApiResponse<CityResponse> findBySlug(@PathVariable String slug) {
        return ApiResponse.success(cityService.findBySlug(slug));
    }

    @GetMapping("/{id}/teachers")
    ApiResponse<PageResponse<TeacherResponse>> teachers(
            @PathVariable Long id,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long programId,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false, defaultValue = "false") boolean nearMe,
            Pageable pageable
    ) {
        return ApiResponse.success(
                teacherService.findAll(null, null, null, id, q, programId, lat, lng, nearMe, pageable)
        );
    }

    @GetMapping("/{id}/centers")
    ApiResponse<PageResponse<CenterResponse>> centers(
            @PathVariable Long id,
            @RequestParam(required = false) CenterType type,
            @RequestParam(required = false) String q,
            Pageable pageable
    ) {
        return ApiResponse.success(centerService.findAll(type, id, q, pageable));
    }

    @GetMapping("/{id}/stats")
    ApiResponse<CommunityStatsResponse> stats(@PathVariable Long id) {
        return ApiResponse.success(cityService.findStats(id));
    }
}

