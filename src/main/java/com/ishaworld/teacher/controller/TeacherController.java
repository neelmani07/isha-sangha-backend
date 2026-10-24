package com.ishaworld.teacher.controller;

import com.ishaworld.common.api.ApiResponse;
import com.ishaworld.common.api.PageResponse;
import com.ishaworld.teacher.dto.TeacherResponse;
import com.ishaworld.teacher.service.TeacherService;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/teachers")
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @GetMapping
    ApiResponse<PageResponse<TeacherResponse>> findAll(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String program,
            Pageable pageable
    ) {
        return ApiResponse.success(teacherService.findAll(city, country, program, null, null, null, null, null, false, pageable));
    }

    @GetMapping("/{id}")
    ApiResponse<TeacherResponse> findById(@PathVariable Long id) {
        return ApiResponse.success(teacherService.findById(id));
    }

    @GetMapping("/slug/{slug}")
    ApiResponse<TeacherResponse> findBySlug(@PathVariable String slug) {
        return ApiResponse.success(teacherService.findBySlug(slug));
    }
}

