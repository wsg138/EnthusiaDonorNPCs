package com.enthusiasmpvp.donornpcs;

import org.bukkit.entity.Player;

import java.util.Collection;

public interface NpcUpdater {
    void setConfig(DonorNpcsConfig config);

    Collection<UpdateStatus> statuses();

    void updateAll(RefreshMode mode);

    void resyncAllViewers();

    void resyncViewer(Player player);
}
