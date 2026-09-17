package de.deinname.epiccrates.listener;

import de.deinname.epiccrates.EpicCrates;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public final class CrateProtectListener implements Listener {
    private final EpicCrates plugin;
    public CrateProtectListener(EpicCrates plugin) { this.plugin = plugin; }
    @EventHandler public void onBreak(BlockBreakEvent event) {
        if (plugin.crates().get(event.getBlock().getLocation()) == null) return;
        if (plugin.getConfig().getBoolean("settings.allow-admin-break", true) && event.getPlayer().hasPermission("epiccrates.admin")) {
            plugin.crates().remove(event.getBlock().getLocation());
        } else { event.setCancelled(true); event.getPlayer().sendMessage("§cDiese Crate ist geschützt."); }
    }
}
