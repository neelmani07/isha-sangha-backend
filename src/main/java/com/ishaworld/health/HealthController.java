package com.ishaworld.health;

import com.ishaworld.common.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @GetMapping
    ApiResponse<HealthResponse> health() {
        return ApiResponse.success(new HealthResponse("UP", "isha-world-backend"));
    }

    public record ClientIpResponse(String seenAs, String forwardedFor) {
    }

    /**
     * Shows callers their own address as the API sees it (the address used for sign-in rate limits). After deploying,
     * open it from a phone: "seenAs" should be the phone's public address, not the host's proxy.
     */
    @GetMapping("/client-ip")
    ApiResponse<ClientIpResponse> clientIp(HttpServletRequest request) {
        return ApiResponse.success(new ClientIpResponse(request.getRemoteAddr(), request.getHeader("X-Forwarded-For")));
    }
}

