package com.ishaworld.admin.controller;

import com.ishaworld.city.dto.CityResponse;
import com.ishaworld.city.dto.CityUpsertRequest;
import com.ishaworld.city.service.CityService;
import com.ishaworld.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/cities")
public class AdminCityController {

    private final CityService cityService;

    public AdminCityController(CityService cityService) {
        this.cityService = cityService;
    }

    @PostMapping
    ApiResponse<CityResponse> create(@Valid @RequestBody CityUpsertRequest request) {
        return ApiResponse.success(cityService.create(request));
    }

    @PutMapping("/{id}")
    ApiResponse<CityResponse> update(@PathVariable Long id, @Valid @RequestBody CityUpsertRequest request) {
        return ApiResponse.success(cityService.update(id, request));
    }

    @DeleteMapping("/{id}")
    ApiResponse<Void> delete(@PathVariable Long id) {
        cityService.delete(id);
        return ApiResponse.success(null);
    }
}

