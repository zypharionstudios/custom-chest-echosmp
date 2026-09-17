package de.deinname.epiccrates;

import de.deinname.epiccrates.animation.CrateAnimation;
import de.deinname.epiccrates.commands.CrateCommand;
import de.deinname.epiccrates.commands.CrateTabCompleter;
import de.deinname.epiccrates.crate.CrateManager;
import de.deinname.epiccrates.listener.CrateInteractListener;
import de.deinname.epiccrates.listener.CrateProtectListener;
import de.deinname.epiccrates.reward.RewardManager;
import java.util.Objects;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public final class EpicCrates extends JavaPlugin {
    private CrateManager crateManager;
    private RewardManager rewardManager;
    private CrateAnimation animation;
    private NamespacedKey crateKey;
    private NamespacedKey crateLabelKey;

    @Override public void onEnable() {
        saveDefaultConfig();
        saveResource("messages.yml", false);
        crateKey = new NamespacedKey(this, "crate_type");
        crateLabelKey = new NamespacedKey(this, "crate_label");
        crateManager = new CrateManager(this);
        crateManager.load();
        rewardManager = new RewardManager(this);
        animation = new CrateAnimation(this);
        CrateCommand command = new CrateCommand(this);
        Objects.requireNonNull(getCommand("crate")).setExecutor(command);
        Objects.requireNonNull(getCommand("crate")).setTabCompleter(new CrateTabCompleter(this));
        getServer().getPluginManager().registerEvents(new CrateInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new CrateProtectListener(this), this);
        getLogger().info("EpicCrates wurde aktiviert.");
    }

    @Override public void onDisable() { if (crateManager != null) crateManager.save(); if (animation != null) animation.cancelAll(); }
    public CrateManager crates() { return crateManager; }
    public RewardManager rewards() { return rewardManager; }
    public CrateAnimation animation() { return animation; }
    public NamespacedKey crateKey() { return crateKey; }
    public NamespacedKey crateLabelKey() { return crateLabelKey; }
}
