package com.enthusiasmpvp.donornpcs;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public final class EnthusiaDonorNPCsPlugin extends JavaPlugin {
    private DonorNpcsConfig donorNpcsConfig;
    private NpcUpdater updater;
    private BukkitTask identityTask;
    private BukkitTask repeatingTask;
    private final List<BukkitTask> recoveryTasks = new ArrayList<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();

        if (!requireEnabledPlugin("PlaceholderAPI")) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        reloadPluginConfig();
        if (!isEnabled()) {
            return;
        }
        registerCommand();
        getServer().getPluginManager().registerEvents(new ViewerResyncListener(this), this);
        scheduleUpdates();

        getLogger().info("Enabled with " + donorNpcsConfig.entries().size() + " configured donor NPC position(s).");
    }

    @Override
    public void onDisable() {
        cancelTasks();
    }

    public NpcUpdater updater() {
        return updater;
    }

    public DonorNpcsConfig donorNpcsConfig() {
        return donorNpcsConfig;
    }

    public void reloadPluginConfig() {
        reloadConfig();
        donorNpcsConfig = DonorNpcsConfig.from(getConfig());

        NpcProvider provider = donorNpcsConfig.npcProvider();
        if (!requireEnabledPlugin(provider == NpcProvider.CITIZENS ? "Citizens" : "FancyNpcs")) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (updater == null || (provider == NpcProvider.CITIZENS) != (updater instanceof DonorNpcUpdater)) {
            updater = provider == NpcProvider.CITIZENS
                    ? new DonorNpcUpdater(this, donorNpcsConfig)
                    : new FancyNpcUpdater(this, donorNpcsConfig);
        } else {
            updater.setConfig(donorNpcsConfig);
        }

        scheduleUpdates();
    }

    public void forceUpdate() {
        if (updater != null) {
            updater.updateAll(RefreshMode.FORCE);
        }
    }

    public void reconcileNow() {
        if (updater != null) {
            updater.updateAll(RefreshMode.RECONCILE);
            updater.resyncAllViewers();
        }
    }

    private boolean requireEnabledPlugin(String pluginName) {
        Plugin dependency = getServer().getPluginManager().getPlugin(pluginName);
        if (dependency == null || !dependency.isEnabled()) {
            getLogger().severe(pluginName + " is required but is not installed or enabled. Disabling EnthusiaDonorNPCs.");
            return false;
        }
        return true;
    }

    private void registerCommand() {
        PluginCommand command = getCommand("EnthusiaDonorNPCs");
        if (command == null) {
            getLogger().severe("Command EnthusiaDonorNPCs is missing from plugin.yml.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        DonorNpcsCommand executor = new DonorNpcsCommand(this);
        command.setExecutor(executor);
        command.setTabCompleter(executor);
    }

    private void scheduleUpdates() {
        if (!isEnabled() || donorNpcsConfig == null || updater == null) {
            return;
        }

        cancelTasks();

        long identityTicks = donorNpcsConfig.identityCheckIntervalSeconds() * 20L;
        long intervalTicks = donorNpcsConfig.updateIntervalMinutes() * 60L * 20L;
        identityTask = getServer().getScheduler().runTaskTimer(this, () -> updater.updateAll(RefreshMode.NORMAL), identityTicks, identityTicks);
        repeatingTask = getServer().getScheduler().runTaskTimer(this, () -> updater.updateAll(RefreshMode.RECONCILE), intervalTicks, intervalTicks);

        scheduleRecoveryPasses();
    }

    private void cancelTasks() {
        if (identityTask != null) {
            identityTask.cancel();
            identityTask = null;
        }
        if (repeatingTask != null) {
            repeatingTask.cancel();
            repeatingTask = null;
        }
        for (BukkitTask task : recoveryTasks) {
            task.cancel();
        }
        recoveryTasks.clear();
    }

    private void scheduleRecoveryPasses() {
        scheduleRecoveryPass(10L * 20L);
        scheduleRecoveryPass(30L * 20L);
        scheduleRecoveryPass(60L * 20L);
    }

    private void scheduleRecoveryPass(long delayTicks) {
        BukkitTask task = getServer().getScheduler().runTaskLater(this, this::reconcileNow, delayTicks);
        recoveryTasks.add(task);
    }
}
