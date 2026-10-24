package com.ishaworld.admin.controller;

import com.ishaworld.common.api.ApiResponse;
import com.ishaworld.teacher.dto.TeacherResponse;
import com.ishaworld.teacher.dto.TeacherUpsertRequest;
import com.ishaworld.teacher.service.TeacherService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/teachers")
public class AdminTeacherController {

    private final TeacherService teacherService;

    public AdminTeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @PostMapping
    ApiResponse<TeacherResponse> create(@Valid @RequestBody TeacherUpsertRequest request) {
        return ApiResponse.success(teacherService.create(request));
    }

    @PutMapping("/{id}")
    ApiResponse<TeacherResponse> update(@PathVariable Long id, @Valid @RequestBody TeacherUpsertRequest request) {
        return ApiResponse.success(teacherService.update(id, request));
    }

    @DeleteMapping("/{id}")
    ApiResponse<Void> delete(@PathVariable Long id) {
        teacherService.delete(id);
        return ApiResponse.success(null);
    }
}

