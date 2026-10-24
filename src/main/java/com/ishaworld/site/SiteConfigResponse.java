package com.ishaworld.site;

public record SiteConfigResponse(
        String name,
        String tagline,
        String contactEmail,
        SiteProperties.Organization organization,
        String disclaimer,
        String googleClientId
) {
}
