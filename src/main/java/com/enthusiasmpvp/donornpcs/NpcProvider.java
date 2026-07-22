package com.enthusiasmpvp.donornpcs;

public enum NpcProvider {
    CITIZENS,
    FANCY_NPCS;

    public static NpcProvider fromConfig(String value) {
        return "fancynpcs".equalsIgnoreCase(value == null ? "" : value.trim())
                ? FANCY_NPCS
                : CITIZENS;
    }

}
