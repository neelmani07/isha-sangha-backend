package com.ishaworld.common;

import java.text.Normalizer;
import java.util.Locale;

public final class SlugGenerator {

    private SlugGenerator() {
    }

    public static String from(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }
}

