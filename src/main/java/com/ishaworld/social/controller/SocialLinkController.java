package com.ishaworld.social.controller;

import com.ishaworld.common.api.ApiResponse;
import com.ishaworld.common.enums.EntityType;
import com.ishaworld.social.dto.SocialLinkResponse;
import com.ishaworld.social.service.SocialLinkService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/social-links")
public class SocialLinkController {

    private final SocialLinkService socialLinkService;

    public SocialLinkController(SocialLinkService socialLinkService) {
        this.socialLinkService = socialLinkService;
    }

    @GetMapping
    ApiResponse<List<SocialLinkResponse>> findForEntity(@RequestParam EntityType entityType, @RequestParam Long entityId) {
        return ApiResponse.success(socialLinkService.findForEntity(entityType, entityId));
    }
}

