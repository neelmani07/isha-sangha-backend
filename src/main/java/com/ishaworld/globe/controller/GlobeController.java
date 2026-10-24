package com.ishaworld.globe.controller;

import com.ishaworld.common.api.ApiResponse;
import com.ishaworld.globe.dto.GlobeMarkerResponse;
import com.ishaworld.globe.service.GlobeService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/globe")
public class GlobeController {

    private final GlobeService globeService;

    public GlobeController(GlobeService globeService) {
        this.globeService = globeService;
    }

    @GetMapping("/markers")
    ApiResponse<List<GlobeMarkerResponse>> markers(
            @RequestParam(defaultValue = "country") String level,
            @RequestParam(required = false) String type
    ) {
        return ApiResponse.success(globeService.findMarkers(level, type));
    }
}

