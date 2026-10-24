package com.ishaworld.resourcelink.controller;

import com.ishaworld.common.api.ApiResponse;
import com.ishaworld.common.enums.ResourceCategory;
import com.ishaworld.resourcelink.dto.ResourceLinkResponse;
import com.ishaworld.resourcelink.service.ResourceLinkService;
import com.ishaworld.security.AuthenticatedUser;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ResourceLinkController {

    private final ResourceLinkService service;

    public ResourceLinkController(ResourceLinkService service) {
        this.service = service;
    }

    @GetMapping("/api/v1/resources")
    ApiResponse<List<ResourceLinkResponse>> list(
            @RequestParam(required = false) ResourceCategory category,
            @RequestParam(required = false) String q,
            @AuthenticationPrincipal AuthenticatedUser viewer
    ) {
        return ApiResponse.success(service.findPublic(category, q, viewer == null ? null : viewer.id()));
    }

    @PostMapping("/api/v1/me/resources/{id}/helpful")
    ApiResponse<ResourceLinkResponse> helpful(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.success(service.setHelpful(user.id(), id, true));
    }

    @DeleteMapping("/api/v1/me/resources/{id}/helpful")
    ApiResponse<ResourceLinkResponse> notHelpful(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.success(service.setHelpful(user.id(), id, false));
    }
}
