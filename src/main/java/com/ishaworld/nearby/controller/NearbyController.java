package com.ishaworld.nearby.controller;

import com.ishaworld.common.api.ApiResponse;
import com.ishaworld.nearby.dto.NearbyResultResponse;
import com.ishaworld.nearby.service.NearbyService;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/nearby")
public class NearbyController {

    private final NearbyService nearbyService;

    public NearbyController(NearbyService nearbyService) {
        this.nearbyService = nearbyService;
    }

    @GetMapping
    ApiResponse<java.util.List<NearbyResultResponse>> findNearby(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "100") double radiusKm,
            @RequestParam(required = false) String types,
            @RequestParam(defaultValue = "50") int limit
    ) {
        Set<String> typeSet = types == null || types.isBlank()
                ? Set.of()
                : Arrays.stream(types.split(","))
                        .map(String::trim)
                        .map(String::toUpperCase)
                        .collect(Collectors.toSet());

        return ApiResponse.success(nearbyService.findNearby(lat, lng, radiusKm, typeSet, limit));
    }
}
