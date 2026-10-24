package com.ishaworld.country.controller;

import com.ishaworld.common.api.ApiResponse;
import com.ishaworld.common.api.PageResponse;
import com.ishaworld.country.dto.CountryResponse;
import com.ishaworld.country.service.CountryService;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/countries")
public class CountryController {

    private final CountryService countryService;

    public CountryController(CountryService countryService) {
        this.countryService = countryService;
    }

    @GetMapping
    ApiResponse<PageResponse<CountryResponse>> findAll(Pageable pageable) {
        return ApiResponse.success(countryService.findAll(pageable));
    }

    @GetMapping("/{id}")
    ApiResponse<CountryResponse> findById(@PathVariable Long id) {
        return ApiResponse.success(countryService.findById(id));
    }
}

