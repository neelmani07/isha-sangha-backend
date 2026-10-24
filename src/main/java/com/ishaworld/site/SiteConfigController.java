package com.ishaworld.site;

import com.ishaworld.common.api.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/site-config")
public class SiteConfigController {

    private final SiteProperties siteProperties;
    private final String googleClientId;

    public SiteConfigController(
            SiteProperties siteProperties,
            @Value("${app.auth.google.client-id:}") String googleClientId
    ) {
        this.siteProperties = siteProperties;
        this.googleClientId = googleClientId;
    }

    @GetMapping
    ApiResponse<SiteConfigResponse> siteConfig() {
        return ApiResponse.success(new SiteConfigResponse(
                siteProperties.name(),
                siteProperties.tagline(),
                siteProperties.contactEmail(),
                siteProperties.organization(),
                siteProperties.disclaimer(),
                googleClientId.isBlank() ? null : googleClientId
        ));
    }
}
