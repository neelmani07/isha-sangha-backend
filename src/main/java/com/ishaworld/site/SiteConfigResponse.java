package com.ishaworld.site;

public record SiteConfigResponse(
        String name,
        String tagline,
        String contactEmail,
        SiteProperties.Organization organization,
        String disclaimer,
        String googleClientId,
        /** True while preview sample data is on the site; the website then shows a notice. */
        boolean sampleData
) {
}
