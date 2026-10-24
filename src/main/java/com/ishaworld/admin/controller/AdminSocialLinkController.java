package com.ishaworld.admin.controller;

import com.ishaworld.common.api.ApiResponse;
import com.ishaworld.social.dto.SocialLinkResponse;
import com.ishaworld.social.dto.SocialLinkUpsertRequest;
import com.ishaworld.social.service.SocialLinkService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/social-links")
public class AdminSocialLinkController {

    private final SocialLinkService socialLinkService;

    public AdminSocialLinkController(SocialLinkService socialLinkService) {
        this.socialLinkService = socialLinkService;
    }

    @PostMapping
    ApiResponse<SocialLinkResponse> create(@Valid @RequestBody SocialLinkUpsertRequest request) {
        return ApiResponse.success(socialLinkService.create(request));
    }

    @PutMapping("/{id}")
    ApiResponse<SocialLinkResponse> update(@PathVariable Long id, @Valid @RequestBody SocialLinkUpsertRequest request) {
        return ApiResponse.success(socialLinkService.update(id, request));
    }

    @DeleteMapping("/{id}")
    ApiResponse<Void> delete(@PathVariable Long id) {
        socialLinkService.delete(id);
        return ApiResponse.success(null);
    }
}

