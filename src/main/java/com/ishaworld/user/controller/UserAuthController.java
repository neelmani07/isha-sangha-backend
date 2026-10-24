package com.ishaworld.user.controller;

import com.ishaworld.common.api.ApiResponse;
import com.ishaworld.security.AuthenticatedUser;
import com.ishaworld.user.dto.AuthResponse;
import com.ishaworld.user.dto.GoogleLoginRequest;
import com.ishaworld.user.dto.OtpRequest;
import com.ishaworld.user.dto.OtpSentResponse;
import com.ishaworld.user.dto.OtpVerifyRequest;
import com.ishaworld.user.dto.UserResponse;
import com.ishaworld.user.service.UserAuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class UserAuthController {

    private final UserAuthService userAuthService;

    public UserAuthController(UserAuthService userAuthService) {
        this.userAuthService = userAuthService;
    }

    @PostMapping("/google")
    ApiResponse<AuthResponse> google(@Valid @RequestBody GoogleLoginRequest request) {
        return ApiResponse.success(userAuthService.loginWithGoogle(request.credential()));
    }

    @PostMapping("/otp/request")
    ApiResponse<OtpSentResponse> requestOtp(@Valid @RequestBody OtpRequest request) {
        return ApiResponse.success(userAuthService.requestOtp(request.email()));
    }

    @PostMapping("/otp/verify")
    ApiResponse<AuthResponse> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        return ApiResponse.success(userAuthService.verifyOtp(request.email(), request.code()));
    }

    @GetMapping("/me")
    ApiResponse<UserResponse> me(@AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.success(userAuthService.currentUser(user.id()));
    }
}
