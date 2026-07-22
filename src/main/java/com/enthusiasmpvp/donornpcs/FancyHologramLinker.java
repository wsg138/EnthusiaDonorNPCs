package com.enthusiasmpvp.donornpcs;

import de.oliver.fancyholograms.api.FancyHologramsPlugin;
import de.oliver.fancyholograms.api.hologram.Hologram;

/** Links preconfigured FancyHolograms text displays to FancyNpcs. */
public final class FancyHologramLinker {
    private final EnthusiaDonorNPCsPlugin plugin;

    public FancyHologramLinker(EnthusiaDonorNPCsPlugin plugin) {
        this.plugin = plugin;
    }

    public void link(LeaderboardEntry entry) {
        if (entry.hologramName().isBlank() || !FancyHologramsPlugin.isEnabled()) {
            return;
        }

        Hologram hologram = FancyHologramsPlugin.get().getHologramManager()
                .getHologram(entry.hologramName())
                .orElse(null);
        if (hologram == null) {
            plugin.getLogger().warning(entry.label() + ": FancyHolograms hologram '"
                    + entry.hologramName() + "' does not exist.");
            return;
        }

        if (!entry.npcId().equals(hologram.getData().getLinkedNpcName())) {
            hologram.getData().setLinkedNpcName(entry.npcId());
            hologram.forceUpdate();
            hologram.queueUpdate();
        }
    }
}
