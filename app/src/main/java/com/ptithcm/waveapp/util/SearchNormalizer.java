package com.ptithcm.waveapp.util;

import java.text.Normalizer;
import java.util.Locale;

public final class SearchNormalizer {

    private SearchNormalizer() {}

    public static String normalize(String value) {
        if (value == null) {
            return "";
        }

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replace('đ', 'd')
                .replace('Đ', 'D');

        return normalized.toLowerCase(Locale.ROOT).trim();
    }

    public static boolean containsNormalized(String source, String query) {
        String normalizedQuery = normalize(query);
        if (normalizedQuery.isEmpty()) {
            return true;
        }
        return normalize(source).contains(normalizedQuery);
    }
}
