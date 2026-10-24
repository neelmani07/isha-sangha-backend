package com.ishaworld.admin.controller;

import com.ishaworld.admin.dto.ManageRow;
import com.ishaworld.admin.service.AdminManageService;
import com.ishaworld.center.dto.CenterResponse;
import com.ishaworld.center.service.CenterService;
import com.ishaworld.city.dto.CityResponse;
import com.ishaworld.city.service.CityService;
import com.ishaworld.common.api.ApiResponse;
import com.ishaworld.teacher.dto.TeacherResponse;
import com.ishaworld.teacher.service.TeacherService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/manage")
public class AdminManageController {

    public record VisibilityRequest(boolean active) {
    }

    private final AdminManageService manageService;
    private final CenterService centerService;
    private final TeacherService teacherService;
    private final CityService cityService;

    public AdminManageController(
            AdminManageService manageService,
            CenterService centerService,
            TeacherService teacherService,
            CityService cityService
    ) {
        this.manageService = manageService;
        this.centerService = centerService;
        this.teacherService = teacherService;
        this.cityService = cityService;
    }

    @GetMapping("/{kind}")
    ApiResponse<List<ManageRow>> search(@PathVariable String kind, @RequestParam(required = false) String q) {
        return ApiResponse.success(manageService.search(kind, q));
    }

    @PostMapping("/{kind}/{id}/visibility")
    ApiResponse<Void> visibility(@PathVariable String kind, @PathVariable Long id, @RequestBody VisibilityRequest request) {
        manageService.setActive(kind, id, request.active());
        return ApiResponse.success(null);
    }

    @PostMapping("/story/{id}/featured")
    ApiResponse<Void> featured(@PathVariable Long id, @RequestBody VisibilityRequest request) {
        manageService.setFeatured(id, request.active());
        return ApiResponse.success(null);
    }

    /** Full records for the edit forms, whatever their status (public reads only return approved, active entries). */
    @GetMapping("/center/{id}/details")
    ApiResponse<CenterResponse> center(@PathVariable Long id) {
        return ApiResponse.success(centerService.findForAdmin(id));
    }

    @GetMapping("/teacher/{id}/details")
    ApiResponse<TeacherResponse> teacher(@PathVariable Long id) {
        return ApiResponse.success(teacherService.findForAdmin(id));
    }

    @GetMapping("/city/{id}/details")
    ApiResponse<CityResponse> city(@PathVariable Long id) {
        return ApiResponse.success(cityService.findById(id));
    }
}
