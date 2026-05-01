package com.enthusiasmpvp.donornpcs;

import java.util.Optional;
import java.util.UUID;

public final class UuidUtil {
    private UuidUtil() {
    }

    public static Optional<UUID> parseUuid(String value) {
        if (value == null) {
            return Optional.empty();
        }

        String trimmed = value.trim();
        if (trimmed.length() == 32) {
            trimmed = trimmed.substring(0, 8)
                    + "-" + trimmed.substring(8, 12)
                    + "-" + trimmed.substring(12, 16)
                    + "-" + trimmed.substring(16, 20)
                    + "-" + trimmed.substring(20);
        }

        try {
            return Optional.of(UUID.fromString(trimmed));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    public static String undashed(UUID uuid) {
        return uuid.toString().replace("-", "");
    }
}
