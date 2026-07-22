package com.enthusiasmpvp.donornpcs;

import me.clip.placeholderapi.PlaceholderAPI;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

public final class DonorNpcUpdater implements NpcUpdater {
    private final EnthusiaDonorNPCsPlugin plugin;
    private final MojangSkinService mojangSkinService = new MojangSkinService();
    private final Map<String, UpdateStatus> statuses = new LinkedHashMap<>();
    private final Map<String, String> activeIdentityKeys = new LinkedHashMap<>();
    private DonorNpcsConfig config;

    public DonorNpcUpdater(EnthusiaDonorNPCsPlugin plugin, DonorNpcsConfig config) {
        this.plugin = plugin;
        setConfig(config);
    }

    public void setConfig(DonorNpcsConfig config) {
        this.config = config;
        for (LeaderboardEntry entry : config.entries()) {
            statuses.computeIfAbsent(entry.statusKey(), ignored -> new UpdateStatus(entry)).setEntry(entry);
        }
        statuses.keySet().removeIf(key -> config.entries().stream().noneMatch(entry -> entry.statusKey().equals(key)));
        activeIdentityKeys.keySet().removeIf(key -> config.entries().stream().noneMatch(entry -> entry.statusKey().equals(key)));
    }

    public Collection<UpdateStatus> statuses() {
        return statuses.values();
    }

    public void updateAll(RefreshMode mode) {
        if (!plugin.getServer().isPrimaryThread()) {
            plugin.getServer().getScheduler().runTask(plugin, () -> updateAll(mode));
            return;
        }

        for (LeaderboardEntry entry : config.entries()) {
            updateOne(entry, mode);
        }
    }

    public void resyncAllViewers() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            resyncViewer(player);
        }
    }

    public void resyncViewer(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        for (LeaderboardEntry entry : config.entries()) {
            NPC npc = findNpc(entry);
            if (npc == null || !npc.isSpawned() || npc.isHiddenFrom(player)) {
                continue;
            }
            updateViewerPackets(npc, player);
        }
    }

    private void updateOne(LeaderboardEntry entry, RefreshMode mode) {
        UpdateStatus status = statuses.computeIfAbsent(entry.statusKey(), ignored -> new UpdateStatus(entry));

        try {
            ResolvedNpcIdentity identity = resolveIdentity(entry);
            String desiredSkinKey = identity.identityKey();
            NPC npc = findNpc(entry);
            if (npc == null) {
                String message = "Citizens NPC ID '" + entry.npcId() + "' does not exist or is not numeric";
                status.markFailure(identity.placeholderValue(), desiredSkinKey, message);
                plugin.getLogger().warning(entry.label() + ": " + message + ".");
                return;
            }

            faceConfiguredDirection(entry, npc);

            String activeIdentityKey = activeIdentityKeys.get(entry.statusKey());
            boolean updateInProgress = activeIdentityKey != null
                    && activeIdentityKey.equalsIgnoreCase(desiredSkinKey)
                    && !desiredSkinKey.equalsIgnoreCase(status.lastAppliedIdentity());
            if (updateInProgress && !mode.isForce()) {
                status.markSkipped(identity.placeholderValue(), desiredSkinKey, "Update already in progress");
                return;
            }
            boolean identityChanged = !desiredSkinKey.equalsIgnoreCase(status.lastAppliedIdentity());
            boolean pendingDifferentIdentity = activeIdentityKey != null && !activeIdentityKey.equalsIgnoreCase(desiredSkinKey);
            boolean shouldSkip =
                    !mode.isForce()
                    && config.onlyUpdateWhenNameChanges()
                    && !identityChanged
                    && !pendingDifferentIdentity
                    && status.lastSuccessful()
                    && !mode.isMaintenance();
            if (shouldSkip) {
                status.markSkipped(identity.placeholderValue(), desiredSkinKey, "No change");
                if (config.logNoChange()) {
                    plugin.getLogger().info(entry.label() + " unchanged at skin '" + desiredSkinKey + "'.");
                }
                return;
            }

            long revision = status.nextRevision();
            activeIdentityKeys.put(entry.statusKey(), desiredSkinKey);
            if (identity.desiredUuid() != null) {
                applyUuidSkinAsync(entry, npc, status, revision, identity, desiredSkinKey, mode);
            } else {
                applyNameSkin(npc, identity.desiredSkinName());
                status.markSuccess(identity.placeholderValue(), desiredSkinKey, "Updated by name/default skin");
                activeIdentityKeys.remove(entry.statusKey());
                resyncNpcViewers(npc);
                if (config.logUpdates()) {
                    plugin.getLogger().info(entry.label() + " skin updated to '" + identity.desiredSkinName() + "'.");
                }
            }
        } catch (Exception ex) {
            String message = "Failed to update " + entry.label();
            status.markFailure(status.lastPlaceholderValue(), status.lastResolvedIdentity(), message + ": " + ex.getMessage());
            plugin.getLogger().log(Level.WARNING, message + ".", ex);
        }
    }

    private NPC findNpc(LeaderboardEntry entry) {
        try {
            return CitizensAPI.getNPCRegistry().getById(Integer.parseInt(entry.npcId()));
        } catch (NumberFormatException ignored) {
            return null;
        }
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
            fallbackSkinName = PlaceholderNameUtil.cleanOrDefault(
                    entry.namePlaceholder(),
                    namePlaceholderValue,
                    config.defaultSkinName()
            );
            if (desiredUuid == null) {
                placeholderValue = namePlaceholderValue;
                desiredSkinName = fallbackSkinName;
            }
        }

        return new ResolvedNpcIdentity(placeholderValue, fallbackSkinName, desiredSkinName, desiredUuid);
    }

    private void applyUuidSkinAsync(
            LeaderboardEntry entry,
            NPC npc,
            UpdateStatus status,
            long revision,
            ResolvedNpcIdentity identity,
            String desiredSkinKey,
            RefreshMode mode
    ) {
        UUID uuid = identity.desiredUuid();
        status.markSkipped(identity.placeholderValue(), desiredSkinKey, "Fetching UUID skin texture");
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                SkinTexture texture = mojangSkinService.fetchTexture(uuid, mode.isForce());
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    try {
                        if (status.revision() != revision || !desiredSkinKey.equalsIgnoreCase(activeIdentityKeys.get(entry.statusKey()))) {
                            return;
                        }
                        applyUuidSkin(npc, uuid, texture);
                        status.markSuccess(identity.placeholderValue(), desiredSkinKey, "Updated by UUID");
                        activeIdentityKeys.remove(entry.statusKey());
                        resyncNpcViewers(npc);
                        if (config.logUpdates()) {
                            plugin.getLogger().info(entry.label() + " skin updated from UUID '" + uuid + "'.");
                        }
                    } catch (Exception ex) {
                        String message = "Failed to apply UUID skin for " + entry.label() + " using UUID '" + uuid + "'";
                        status.markFailure(identity.placeholderValue(), desiredSkinKey, message + ": " + ex.getMessage());
                        activeIdentityKeys.remove(entry.statusKey());
                        plugin.getLogger().log(Level.WARNING, message + ".", ex);
                    }
                });
            } catch (SkinProfileNotFoundException ex) {
                plugin.getServer().getScheduler().runTask(plugin, () ->
                        applyFallbackSkin(entry, npc, status, revision, identity, desiredSkinKey, uuid, ex.getMessage()));
            } catch (IOException | InterruptedException ex) {
                if (ex instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                plugin.getServer().getScheduler().runTask(plugin, () ->
                        applyFallbackSkin(entry, npc, status, revision, identity, desiredSkinKey, uuid, ex.getMessage()));
            } catch (Exception ex) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    String message = "Failed to fetch UUID skin for " + entry.label() + " using UUID '" + uuid + "'";
                    status.markFailure(identity.placeholderValue(), desiredSkinKey, message + ": " + ex.getMessage());
                    activeIdentityKeys.remove(entry.statusKey());
                    plugin.getLogger().log(Level.WARNING, message + ".", ex);
                });
            }
        });
    }

    private void applyFallbackSkin(
            LeaderboardEntry entry,
            NPC npc,
            UpdateStatus status,
            long revision,
            ResolvedNpcIdentity identity,
            String desiredSkinKey,
            UUID uuid,
            String reason
    ) {
        try {
            if (status.revision() != revision || !desiredSkinKey.equalsIgnoreCase(activeIdentityKeys.get(entry.statusKey()))) {
                return;
            }
            applyNameSkin(npc, identity.fallbackSkinName());
            status.markSuccess(identity.placeholderValue(), desiredSkinKey, "UUID skin unavailable; used fallback skin '" + identity.fallbackSkinName() + "'");
            activeIdentityKeys.remove(entry.statusKey());
            resyncNpcViewers(npc);
            plugin.getLogger().warning(entry.label()
                    + ": could not use UUID skin '" + uuid + "' (" + reason
                    + "), so fallback skin '" + identity.fallbackSkinName() + "' was applied. "
                    + "If you want exact UUID skins, make sure the placeholder returns an online-mode Mojang UUID.");
        } catch (Exception ex) {
            String message = "Failed to apply fallback skin for " + entry.label() + " after UUID skin fetch failed";
            status.markFailure(identity.placeholderValue(), desiredSkinKey, message + ": " + ex.getMessage());
            activeIdentityKeys.remove(entry.statusKey());
            plugin.getLogger().log(Level.WARNING, message + ".", ex);
        }
    }

    private void applyUuidSkin(NPC npc, UUID uuid, SkinTexture texture) {
        SkinTrait skinTrait = npc.getOrAddTrait(SkinTrait.class);

        // UUID placeholders avoid name-history ambiguity. We fetch the signed Mojang texture
        // off the main thread, then persist the exact texture onto the Citizens SkinTrait.
        skinTrait.setFetchDefaultSkin(false);
        skinTrait.setShouldUpdateSkins(true);
        skinTrait.setSkinPersistent(uuid.toString(), texture.signature(), texture.value());
    }

    private void applyNameSkin(NPC npc, String skinName) {
        SkinTrait skinTrait = npc.getOrAddTrait(SkinTrait.class);

        // Citizens owns the profile lookup/cache. Passing forceUpdate=true asks it to fetch
        // the named player's skin data when it has not already been loaded.
        skinTrait.setFetchDefaultSkin(false);
        skinTrait.setShouldUpdateSkins(true);
        skinTrait.setSkinName(skinName, true);
    }

    private void faceConfiguredDirection(LeaderboardEntry entry, NPC npc) {
        if (!npc.isSpawned()) {
            return;
        }

        Location location = currentOrStoredLocation(npc);
        if (location == null) {
            return;
        }

        FacingDirection facingDirection = entry.facingDirection();
        setRotation(location, facingDirection);
        npc.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);

        Location faceTarget = location.clone().add(
                facingDirection.targetXOffset(),
                facingDirection.targetYOffset(),
                facingDirection.targetZOffset()
        );
        npc.faceLocation(faceTarget);
    }

    private void setRotation(Location location, FacingDirection facingDirection) {
        location.setYaw(facingDirection.yaw());
        location.setPitch(0.0F);
    }

    private Location currentOrStoredLocation(NPC npc) {
        Entity entity = npc.getEntity();
        if (entity != null) {
            return entity.getLocation();
        }
        return npc.getStoredLocation();
    }

    private void resyncNpcViewers(NPC npc) {
        if (!npc.isSpawned()) {
            return;
        }
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (!npc.isHiddenFrom(player)) {
                updateViewerPackets(npc, player);
            }
        }
    }

    private void updateViewerPackets(NPC npc, Player player) {
        Entity entity = npc.getEntity();
        if (!(entity instanceof SkinnableEntity skinnableEntity)) {
            return;
        }
        skinnableEntity.getSkinTracker().updateViewer(player);
    }
}
