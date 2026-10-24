package com.ishaworld.admin.controller;

import com.ishaworld.common.api.ApiResponse;
import com.ishaworld.program.dto.ProgramResponse;
import com.ishaworld.program.dto.ProgramUpsertRequest;
import com.ishaworld.program.service.ProgramService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/programs")
public class AdminProgramController {

    private final ProgramService programService;

    public AdminProgramController(ProgramService programService) {
        this.programService = programService;
    }

    @PostMapping
    ApiResponse<ProgramResponse> create(@Valid @RequestBody ProgramUpsertRequest request) {
        return ApiResponse.success(programService.create(request));
    }

    @PutMapping("/{id}")
    ApiResponse<ProgramResponse> update(@PathVariable Long id, @Valid @RequestBody ProgramUpsertRequest request) {
        return ApiResponse.success(programService.update(id, request));
    }

    @DeleteMapping("/{id}")
    ApiResponse<Void> delete(@PathVariable Long id) {
        programService.delete(id);
        return ApiResponse.success(null);
    }
}

