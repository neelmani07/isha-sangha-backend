package com.ishaworld.user.controller;

import com.ishaworld.common.api.ApiResponse;
import com.ishaworld.exception.TooManyRequestsException;
import com.ishaworld.security.AuthenticatedUser;
import com.ishaworld.security.RequestRateLimiter;
import com.ishaworld.user.dto.AuthResponse;
import com.ishaworld.user.dto.GoogleLoginRequest;
import com.ishaworld.user.dto.OtpRequest;
import com.ishaworld.user.dto.OtpSentResponse;
import com.ishaworld.user.dto.OtpVerifyRequest;
import com.ishaworld.user.dto.UserResponse;
import com.ishaworld.user.service.UserAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.Duration;
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
    private final RequestRateLimiter rateLimiter;

    public UserAuthController(UserAuthService userAuthService, RequestRateLimiter rateLimiter) {
        this.userAuthService = userAuthService;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping("/google")
    ApiResponse<AuthResponse> google(@Valid @RequestBody GoogleLoginRequest request) {
        return ApiResponse.success(userAuthService.loginWithGoogle(request.credential()));
    }

    @PostMapping("/otp/request")
    ApiResponse<OtpSentResponse> requestOtp(@Valid @RequestBody OtpRequest request, HttpServletRequest http) {
        // Per network address, on top of the per-email limits in the service: stops one visitor
        // spraying codes at many different addresses (which costs email credits and annoys people).
        String ip = http.getRemoteAddr();
        if (!rateLimiter.tryAcquire("otp-min:" + ip, 3, Duration.ofMinutes(1))
                || !rateLimiter.tryAcquire("otp-hour:" + ip, 20, Duration.ofHours(1))) {
            throw new TooManyRequestsException("Too many sign-in codes requested from your network. Please try again later.");
        }
        return ApiResponse.success(userAuthService.requestOtp(request.email()));
    }

    @PostMapping("/otp/verify")
    ApiResponse<AuthResponse> verifyOtp(@Valid @RequestBody OtpVerifyRequest request, HttpServletRequest http) {
        if (!rateLimiter.tryAcquire("otp-verify:" + http.getRemoteAddr(), 30, Duration.ofMinutes(10))) {
            throw new TooManyRequestsException("Too many attempts. Please wait a few minutes and try again.");
        }
        return ApiResponse.success(userAuthService.verifyOtp(request.email(), request.code()));
    }

    @GetMapping("/me")
    ApiResponse<UserResponse> me(@AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.success(userAuthService.currentUser(user.id()));
    }
}
