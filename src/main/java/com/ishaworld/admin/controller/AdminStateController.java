package com.ishaworld.admin.controller;

import com.ishaworld.common.api.ApiResponse;
import com.ishaworld.state.dto.StateResponse;
import com.ishaworld.state.dto.StateUpsertRequest;
import com.ishaworld.state.service.StateService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/states")
public class AdminStateController {

    private final StateService stateService;

    public AdminStateController(StateService stateService) {
        this.stateService = stateService;
    }

    @PostMapping
    ApiResponse<StateResponse> create(@Valid @RequestBody StateUpsertRequest request) {
        return ApiResponse.success(stateService.create(request));
    }

    @PutMapping("/{id}")
    ApiResponse<StateResponse> update(@PathVariable Long id, @Valid @RequestBody StateUpsertRequest request) {
        return ApiResponse.success(stateService.update(id, request));
    }

    @DeleteMapping("/{id}")
    ApiResponse<Void> delete(@PathVariable Long id) {
        stateService.delete(id);
        return ApiResponse.success(null);
    }
}

