package de.deinname.epiccrates.listener;

import de.deinname.epiccrates.EpicCrates;
import de.deinname.epiccrates.commands.CrateCommand;
import de.deinname.epiccrates.crate.Crate;
import de.deinname.epiccrates.crate.CrateType;
import de.deinname.epiccrates.util.ColorUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public final class CrateInteractListener implements Listener {
    private final EpicCrates plugin;
    public CrateInteractListener(EpicCrates plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        String marker = event.getItemInHand().getItemMeta() == null ? null : event.getItemInHand().getItemMeta().getPersistentDataContainer().get(plugin.crateKey(), PersistentDataType.STRING);
        if (marker == null || !marker.startsWith("crate:")) return;
        CrateType type = CrateType.fromId(marker.substring(6)); if (type != null) { plugin.crates().create(event.getBlock().getLocation(), type, event.getPlayer().getUniqueId()); event.getPlayer().sendMessage("§a" + type.id() + " wurde platziert."); }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Crate crate = plugin.crates().get(event.getClickedBlock().getLocation()); if (crate == null) return;
        event.setCancelled(true); Player player = event.getPlayer();
        if (!player.hasPermission("epiccrates.use")) { player.sendMessage("§cDafür fehlt dir die Berechtigung."); return; }
        if (plugin.animation().isActive(crate)) { player.sendMessage("§cDiese Crate wird gerade geöffnet."); return; }
        long cooldown = plugin.animation().cooldown(player.getUniqueId()); if (cooldown > 0) { player.sendMessage("§cDu musst noch §e" + ((cooldown + 999) / 1000) + "s §cwarten."); return; }
        ItemStack key = findKey(player, crate.type());
        if (plugin.getConfig().getBoolean("settings.key-required", true) && key == null) { player.sendMessage(ColorUtil.replace("§cDu brauchst einen §e%type% Key§c!", "%type%", crate.type().id())); return; }
        if (key != null) key.setAmount(key.getAmount() - 1);
        plugin.animation().start(player, crate);
    }

    private ItemStack findKey(Player player, CrateType type) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) continue;
            String marker = item.getItemMeta().getPersistentDataContainer().get(plugin.crateKey(), PersistentDataType.STRING);
            if (("key:" + type.id()).equals(marker)) return item;
        }
        return null;
    }
}
