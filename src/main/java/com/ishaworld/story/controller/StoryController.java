package com.ishaworld.story.controller;

import com.ishaworld.common.api.ApiResponse;
import com.ishaworld.common.enums.StoryCategory;
import com.ishaworld.security.AuthenticatedUser;
import com.ishaworld.story.dto.StoryResponse;
import com.ishaworld.story.service.StoryService;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StoryController {

    private final StoryService storyService;

    public StoryController(StoryService storyService) {
        this.storyService = storyService;
    }

    @GetMapping("/api/v1/stories")
    ApiResponse<List<StoryResponse>> list(
            @RequestParam(required = false) StoryCategory category,
            @AuthenticationPrincipal AuthenticatedUser viewer
    ) {
        return ApiResponse.success(storyService.findPublic(category, viewer == null ? null : viewer.id()));
    }

    @GetMapping("/api/v1/stories/slug/{slug}")
    ApiResponse<StoryResponse> bySlug(@PathVariable String slug, @AuthenticationPrincipal AuthenticatedUser viewer) {
        return ApiResponse.success(storyService.findPublicBySlug(slug, viewer == null ? null : viewer.id()));
    }

    @PostMapping("/api/v1/me/stories/{id}/like")
    ApiResponse<StoryResponse> like(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.success(storyService.setLiked(user.id(), id, true));
    }

    @DeleteMapping("/api/v1/me/stories/{id}/like")
    ApiResponse<StoryResponse> unlike(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.success(storyService.setLiked(user.id(), id, false));
    }
}
