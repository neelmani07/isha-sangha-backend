package com.ishaworld.city.controller;

import com.ishaworld.city.dto.MemberCityRequest;
import com.ishaworld.city.dto.PickedCityResponse;
import com.ishaworld.city.service.MemberCityService;
import com.ishaworld.common.api.ApiResponse;
import com.ishaworld.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MemberCityController {

    private final MemberCityService memberCityService;

    public MemberCityController(MemberCityService memberCityService) {
        this.memberCityService = memberCityService;
    }

    @PostMapping("/api/v1/me/cities")
    ApiResponse<PickedCityResponse> addCity(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody MemberCityRequest request) {
        return ApiResponse.success(memberCityService.addOrFind(user.id(), request));
    }
}
