package com.ishaworld.admin.controller;

import com.ishaworld.common.api.ApiResponse;
import com.ishaworld.country.dto.CountryResponse;
import com.ishaworld.country.dto.CountryUpsertRequest;
import com.ishaworld.country.service.CountryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/countries")
public class AdminCountryController {

    private final CountryService countryService;

    public AdminCountryController(CountryService countryService) {
        this.countryService = countryService;
    }

    @PostMapping
    ApiResponse<CountryResponse> create(@Valid @RequestBody CountryUpsertRequest request) {
        return ApiResponse.success(countryService.create(request));
    }

    @PutMapping("/{id}")
    ApiResponse<CountryResponse> update(@PathVariable Long id, @Valid @RequestBody CountryUpsertRequest request) {
        return ApiResponse.success(countryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    ApiResponse<Void> delete(@PathVariable Long id) {
        countryService.delete(id);
        return ApiResponse.success(null);
    }
}

