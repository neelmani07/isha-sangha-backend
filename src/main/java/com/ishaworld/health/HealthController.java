package com.ishaworld.health;

import com.ishaworld.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @GetMapping
    ApiResponse<HealthResponse> health() {
        return ApiResponse.success(new HealthResponse("UP", "isha-world-backend"));
    }
}

