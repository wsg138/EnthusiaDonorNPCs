package com.enthusiasmpvp.donornpcs;

import de.oliver.fancynpcs.api.FancyNpcsPlugin;
import de.oliver.fancynpcs.api.Npc;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

/** Updates skins through FancyNpcs' supported API. */
public final class FancyNpcUpdater implements NpcUpdater {
    private final EnthusiaDonorNPCsPlugin plugin;
    private final FancyHologramLinker hologramLinker;
    private final Map<String, UpdateStatus> statuses = new LinkedHashMap<>();
    private DonorNpcsConfig config;

    public FancyNpcUpdater(EnthusiaDonorNPCsPlugin plugin, DonorNpcsConfig config) {
        this.plugin = plugin;
        this.hologramLinker = new FancyHologramLinker(plugin);
        setConfig(config);
    }

    @Override
    public void setConfig(DonorNpcsConfig config) {
        this.config = config;
        for (LeaderboardEntry entry : config.entries()) {
            statuses.computeIfAbsent(entry.statusKey(), ignored -> new UpdateStatus(entry)).setEntry(entry);
        }
        statuses.keySet().removeIf(key -> config.entries().stream().noneMatch(entry -> entry.statusKey().equals(key)));
    }

    @Override
    public Collection<UpdateStatus> statuses() {
        return statuses.values();
    }

    @Override
    public void updateAll(RefreshMode mode) {
        if (!plugin.getServer().isPrimaryThread()) {
            plugin.getServer().getScheduler().runTask(plugin, () -> updateAll(mode));
            return;
        }
        for (LeaderboardEntry entry : config.entries()) {
            updateOne(entry, mode);
        }
    }

    @Override
    public void resyncAllViewers() {
        // FancyNpcs owns packet visibility and refreshes every shown viewer via updateForAll().
    }

    @Override
    public void resyncViewer(Player player) {
        // FancyNpcs handles joining, world changes, and per-player visibility itself.
    }

    private void updateOne(LeaderboardEntry entry, RefreshMode mode) {
        UpdateStatus status = statuses.computeIfAbsent(entry.statusKey(), ignored -> new UpdateStatus(entry));
        try {
            ResolvedNpcIdentity identity = resolveIdentity(entry);
            String desiredSkinKey = identity.identityKey();
            Npc npc = findNpc(entry.npcId());
            if (npc == null) {
                String message = "FancyNpcs NPC '" + entry.npcId() + "' does not exist";
                status.markFailure(identity.placeholderValue(), desiredSkinKey, message);
                plugin.getLogger().warning(entry.label() + ": " + message + ".");
                return;
            }

            hologramLinker.link(entry);

            boolean identityChanged = !desiredSkinKey.equalsIgnoreCase(status.lastAppliedIdentity());
            if (!mode.isForce() && config.onlyUpdateWhenNameChanges() && !identityChanged
                    && status.lastSuccessful() && !mode.isMaintenance()) {
                status.markSkipped(identity.placeholderValue(), desiredSkinKey, "No change");
                return;
            }

            faceConfiguredDirection(entry, npc);
            npc.getData().setSkin(identity.desiredSkinName());
            npc.updateForAll();
            status.markSuccess(identity.placeholderValue(), desiredSkinKey, "Updated through FancyNpcs");
            if (config.logUpdates()) {
                plugin.getLogger().info(entry.label() + " skin updated to '" + identity.desiredSkinName() + "'.");
            }
        } catch (Exception ex) {
            String message = "Failed to update " + entry.label();
            status.markFailure(status.lastPlaceholderValue(), status.lastResolvedIdentity(), message + ": " + ex.getMessage());
            plugin.getLogger().log(Level.WARNING, message + ".", ex);
        }
    }

    private Npc findNpc(String identifier) {
        try {
            Npc numericNpc = FancyNpcsPlugin.get().getNpcManager().getNpc(Integer.parseInt(identifier));
            if (numericNpc != null) {
                return numericNpc;
            }
        } catch (NumberFormatException ignored) {
            // Names and UUID-like FancyNpcs IDs are handled below.
        }
        Npc npc = FancyNpcsPlugin.get().getNpcManager().getNpc(identifier);
        return npc != null ? npc : FancyNpcsPlugin.get().getNpcManager().getNpcById(identifier);
    }

    private ResolvedNpcIdentity resolveIdentity(LeaderboardEntry entry) {
        String placeholderValue = "";
        String desiredSkinName = config.defaultSkinName();
        String fallbackSkinName = config.defaultSkinName();
        UUID desiredUuid = null;

        if (!entry.uuidPlaceholder().isBlank()) {
            placeholderValue = PlaceholderAPI.setPlaceholders((org.bukkit.OfflinePlayer) null, entry.uuidPlaceholder());
            String uuidValue = PlaceholderNameUtil.cleanUuidValue(entry.uuidPlaceholder(), placeholderValue);
            Optional<UUID> parsedUuid = UuidUtil.parseUuid(uuidValue);
            if (parsedUuid.isPresent()) {
                desiredUuid = parsedUuid.get();
                desiredSkinName = desiredUuid.toString();
            } else if (!uuidValue.isBlank()) {
                plugin.getLogger().warning(entry.label() + ": UUID placeholder returned an invalid UUID: '" + uuidValue + "'. Falling back to name/default skin.");
            }
        }

        if (!entry.namePlaceholder().isBlank()) {
            String namePlaceholderValue = PlaceholderAPI.setPlaceholders((org.bukkit.OfflinePlayer) null, entry.namePlaceholder());
            fallbackSkinName = PlaceholderNameUtil.cleanOrDefault(entry.namePlaceholder(), namePlaceholderValue, config.defaultSkinName());
            if (desiredUuid == null) {
                placeholderValue = namePlaceholderValue;
                desiredSkinName = fallbackSkinName;
            }
        }
        return new ResolvedNpcIdentity(placeholderValue, fallbackSkinName, desiredSkinName, desiredUuid);
    }

    private void faceConfiguredDirection(LeaderboardEntry entry, Npc npc) {
        Location location = npc.getData().getLocation();
        if (location == null) {
            return;
        }
        FacingDirection facingDirection = entry.facingDirection();
        location = location.clone();
        location.setYaw(facingDirection.yaw());
        location.setPitch(0.0F);
        npc.getData().setLocation(location);
        npc.moveForAll(true);
    }
}
