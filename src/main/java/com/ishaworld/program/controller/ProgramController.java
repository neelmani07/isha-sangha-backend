package com.ishaworld.program.controller;

import com.ishaworld.common.api.ApiResponse;
import com.ishaworld.common.api.PageResponse;
import com.ishaworld.program.dto.ProgramResponse;
import com.ishaworld.program.service.ProgramService;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/programs")
public class ProgramController {

    private final ProgramService programService;

    public ProgramController(ProgramService programService) {
        this.programService = programService;
    }

    @GetMapping
    ApiResponse<PageResponse<ProgramResponse>> findAll(Pageable pageable) {
        return ApiResponse.success(programService.findAll(pageable));
    }
}

