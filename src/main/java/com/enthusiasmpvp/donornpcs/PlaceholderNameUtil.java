package com.enthusiasmpvp.donornpcs;

import java.util.Locale;

public final class PlaceholderNameUtil {
    private PlaceholderNameUtil() {
    }

    public static String cleanOrDefault(String placeholder, String rawValue, String defaultSkinName) {
        if (isInvalidPlaceholderValue(placeholder, rawValue)) {
            return defaultSkinName;
        }
        return rawValue.trim();
    }

    public static boolean isInvalidPlaceholderValue(String placeholder, String rawValue) {
        if (rawValue == null) {
            return true;
        }

        String trimmed = rawValue.trim();
        if (trimmed.isEmpty()) {
            return true;
        }
        if (placeholder != null && trimmed.equals(placeholder.trim())) {
            return true;
        }

        String normalized = trimmed.toLowerCase(Locale.ROOT);
        return normalized.equals("null")
                || normalized.equals("none")
                || normalized.equals("n/a")
                || normalized.equals("-");
    }

    public static String cleanUuidValue(String placeholder, String rawValue) {
        if (isInvalidPlaceholderValue(placeholder, rawValue)) {
            return "";
        }
        return rawValue.trim();
    }
}
