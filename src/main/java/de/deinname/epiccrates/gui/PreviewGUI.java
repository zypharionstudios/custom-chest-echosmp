package de.deinname.epiccrates.gui;

import de.deinname.epiccrates.EpicCrates;
import de.deinname.epiccrates.crate.CrateType;
import de.deinname.epiccrates.reward.Reward;
import de.deinname.epiccrates.util.ColorUtil;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class PreviewGUI implements Listener {
    private final EpicCrates plugin;
    public PreviewGUI(EpicCrates plugin) { this.plugin = plugin; plugin.getServer().getPluginManager().registerEvents(this, plugin); }
    public void open(Player player, CrateType type) {
        Inventory inventory = Bukkit.createInventory(null, 27, ColorUtil.color("&8Loot: " + type.id()));
        List<Reward> rewards = plugin.rewards().getRewards(type);
        for (int index = 0; index < rewards.size() && index < 27; index++) {
            Reward reward = rewards.get(index); ItemStack item = new ItemStack(reward.material(), reward.amount()); ItemMeta meta = item.getItemMeta(); meta.setDisplayName("§f" + reward.material().name()); meta.setLore(java.util.List.of("§7Seltenheit: §e" + reward.rarity(), "§7Wahrscheinlichkeit: §e" + String.format(java.util.Locale.US, "%.2f", plugin.rewards().probability(reward, type)) + "%")); item.setItemMeta(meta); inventory.setItem(index, item);
        }
        player.openInventory(inventory);
    }
    @EventHandler public void onClick(InventoryClickEvent event) { if (event.getView().getTitle().startsWith("§8Loot: ")) event.setCancelled(true); }
}
