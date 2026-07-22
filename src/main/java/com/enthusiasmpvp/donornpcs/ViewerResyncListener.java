package com.enthusiasmpvp.donornpcs;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public final class ViewerResyncListener implements Listener {
    private final EnthusiaDonorNPCsPlugin plugin;

    public ViewerResyncListener(EnthusiaDonorNPCsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        queueResync(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRespawn(PlayerRespawnEvent event) {
        queueResync(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        queueResync(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.getTo() == null) {
            queueResync(event.getPlayer());
            return;
        }
        if (event.getFrom().getWorld() == event.getTo().getWorld()
                && event.getFrom().distanceSquared(event.getTo()) < 256.0D) {
            return;
        }
        queueResync(event.getPlayer());
    }

    private void queueResync(Player player) {
        int delayTicks = plugin.donorNpcsConfig().viewerResyncDelayTicks();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> plugin.updater().resyncViewer(player), delayTicks);
    }
}
