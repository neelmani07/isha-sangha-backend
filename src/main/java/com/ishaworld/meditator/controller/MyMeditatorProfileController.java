package com.ishaworld.meditator.controller;

import com.ishaworld.common.api.ApiResponse;
import com.ishaworld.meditator.dto.MeditatorProfileRequest;
import com.ishaworld.meditator.dto.MeditatorResponse;
import com.ishaworld.meditator.service.MeditatorService;
import com.ishaworld.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** The signed-in member's own meditator profile. */
@RestController
@RequestMapping("/api/v1/me/meditator")
public class MyMeditatorProfileController {

    private final MeditatorService meditatorService;

    public MyMeditatorProfileController(MeditatorService meditatorService) {
        this.meditatorService = meditatorService;
    }

    @GetMapping
    ApiResponse<MeditatorResponse> get(@AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.success(meditatorService.getOwnProfile(user.id()));
    }

    @PutMapping
    ApiResponse<MeditatorResponse> save(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody MeditatorProfileRequest request
    ) {
        return ApiResponse.success(meditatorService.saveOwnProfile(user.id(), request));
    }

    /** Removes the profile completely (the member's account stays). */
    @DeleteMapping
    ApiResponse<Void> delete(@AuthenticationPrincipal AuthenticatedUser user) {
        meditatorService.deleteOwnProfile(user.id());
        return ApiResponse.success(null);
    }
}
