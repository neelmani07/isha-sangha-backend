package com.ishaworld.state.controller;

import com.ishaworld.common.api.ApiResponse;
import com.ishaworld.common.api.PageResponse;
import com.ishaworld.state.dto.StateResponse;
import com.ishaworld.state.service.StateService;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/states")
public class StateController {

    private final StateService stateService;

    public StateController(StateService stateService) {
        this.stateService = stateService;
    }

    @GetMapping
    ApiResponse<PageResponse<StateResponse>> findAll(@RequestParam(required = false) Long countryId, Pageable pageable) {
        return ApiResponse.success(stateService.findAll(countryId, pageable));
    }

    @GetMapping("/{id}")
    ApiResponse<StateResponse> findById(@PathVariable Long id) {
        return ApiResponse.success(stateService.findById(id));
    }
}

