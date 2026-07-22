package com.enthusiasmpvp.donornpcs;

import java.util.UUID;

public record ResolvedNpcIdentity(
        String placeholderValue,
        String fallbackSkinName,
        String desiredSkinName,
        UUID desiredUuid
) {
    public String identityKey() {
        return desiredUuid == null ? "name:" + desiredSkinName.toLowerCase() : "uuid:" + desiredUuid;
    }

    public String statusValue() {
        return desiredUuid == null ? desiredSkinName : desiredUuid.toString();
    }
}
