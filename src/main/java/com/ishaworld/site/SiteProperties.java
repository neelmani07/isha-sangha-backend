package com.ishaworld.site;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Branding and legal identity of the site. Everything the public sees about who runs it
 * (name, NGO details, disclaimer) comes from here so a rename is a config change only.
 */
@ConfigurationProperties(prefix = "app.site")
public record SiteProperties(
        String name,
        String tagline,
        String contactEmail,
        Organization organization,
        String disclaimer
) {

    public record Organization(
            String legalName,
            String registrationNumber,
            String taxExemption,
            String address
    ) {
    }
}
