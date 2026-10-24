package com.ishaworld.common;

import com.ishaworld.exception.BusinessException;
import java.net.URI;
import java.util.Locale;

/** Checks web addresses that members submit before they are shown to others. */
public final class UrlRules {

    private UrlRules() {
    }

    /** Returns a clean https/http URL, adding https:// when the scheme is missing; null for blank input. */
    public static String normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String value = raw.trim();
        if (!value.matches("(?i)^https?://.*")) {
            value = "https://" + value;
        }
        try {
            URI uri = URI.create(value);
            String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
            if (!(scheme.equals("http") || scheme.equals("https")) || uri.getHost() == null || !uri.getHost().contains(".")) {
                throw new BusinessException("Please enter a valid web address.");
            }
            return uri.toString();
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("Please enter a valid web address.");
        }
    }

    public static String host(String url) {
        try {
            String host = URI.create(url).getHost();
            return host == null ? null : host.toLowerCase(Locale.ROOT).replaceFirst("^www\\.", "");
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
