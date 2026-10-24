package com.ishaworld.initiative.controller;

import com.ishaworld.common.api.ApiResponse;
import com.ishaworld.common.enums.InitiativeKind;
import com.ishaworld.common.enums.InitiativeNeed;
import com.ishaworld.initiative.dto.InitiativeResponse;
import com.ishaworld.initiative.dto.InitiativeStatsResponse;
import com.ishaworld.initiative.service.InitiativeService;
import com.ishaworld.security.AuthenticatedUser;
import java.util.List;
import java.util.Set;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InitiativeController {

    private final InitiativeService initiativeService;

    public InitiativeController(InitiativeService initiativeService) {
        this.initiativeService = initiativeService;
    }

    @GetMapping("/api/v1/initiatives")
    ApiResponse<List<InitiativeResponse>> list(
            @RequestParam(required = false) InitiativeKind kind,
            @RequestParam(required = false) Set<InitiativeNeed> needs,
            @RequestParam(required = false) Long cityId,
            @AuthenticationPrincipal AuthenticatedUser viewer
    ) {
        return ApiResponse.success(initiativeService.findPublic(kind, needs, cityId, viewerId(viewer)));
    }

    @GetMapping("/api/v1/initiatives/stats")
    ApiResponse<InitiativeStatsResponse> stats() {
        return ApiResponse.success(initiativeService.stats());
    }

    @GetMapping("/api/v1/initiatives/slug/{slug}")
    ApiResponse<InitiativeResponse> bySlug(@PathVariable String slug, @AuthenticationPrincipal AuthenticatedUser viewer) {
        return ApiResponse.success(initiativeService.findPublicBySlug(slug, viewerId(viewer)));
    }

    @PostMapping("/api/v1/me/initiatives/{id}/support")
    ApiResponse<InitiativeResponse> support(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.success(initiativeService.setSupport(user.id(), id, true));
    }

    @DeleteMapping("/api/v1/me/initiatives/{id}/support")
    ApiResponse<InitiativeResponse> unsupport(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.success(initiativeService.setSupport(user.id(), id, false));
    }

    /** Anonymous visitors have no principal; admins have a different principal type. */
    static Long viewerId(Object principal) {
        return principal instanceof AuthenticatedUser user ? user.id() : null;
    }
}
