package com.ishaworld.admin.controller;

import com.ishaworld.center.dto.CenterResponse;
import com.ishaworld.center.dto.CenterUpsertRequest;
import com.ishaworld.center.service.CenterService;
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
@RequestMapping("/api/v1/admin/centers")
public class AdminCenterController {

    private final CenterService centerService;

    public AdminCenterController(CenterService centerService) {
        this.centerService = centerService;
    }

    @PostMapping
    ApiResponse<CenterResponse> create(@Valid @RequestBody CenterUpsertRequest request) {
        return ApiResponse.success(centerService.create(request));
    }

    @PutMapping("/{id}")
    ApiResponse<CenterResponse> update(@PathVariable Long id, @Valid @RequestBody CenterUpsertRequest request) {
        return ApiResponse.success(centerService.update(id, request));
    }

    @DeleteMapping("/{id}")
    ApiResponse<Void> delete(@PathVariable Long id) {
        centerService.delete(id);
        return ApiResponse.success(null);
    }
}

