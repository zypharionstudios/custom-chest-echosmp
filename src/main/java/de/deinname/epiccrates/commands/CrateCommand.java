package de.deinname.epiccrates.commands;

import de.deinname.epiccrates.EpicCrates;
import de.deinname.epiccrates.crate.Crate;
import de.deinname.epiccrates.crate.CrateType;
import de.deinname.epiccrates.gui.PreviewGUI;
import de.deinname.epiccrates.util.ColorUtil;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public final class CrateCommand implements CommandExecutor {
    private final EpicCrates plugin;
    public CrateCommand(EpicCrates plugin) { this.plugin = plugin; }

    public static ItemStack createItem(EpicCrates plugin, CrateType type, boolean key, int amount) {
        Material material = key ? Material.matchMaterial(plugin.getConfig().getString("crates." + type.id() + ".key-material", "TRIPWIRE_HOOK")) : Material.CHEST;
        ItemStack item = new ItemStack(material == null ? Material.CHEST : material, Math.max(1, Math.min(64, amount)));
        ItemMeta meta = item.getItemMeta();
        String display = plugin.getConfig().getString("crates." + type.id() + ".display-name", type.id());
        meta.setDisplayName(ColorUtil.color((key ? "&e" + type.id() + " Key" : display)));
        meta.getPersistentDataContainer().set(plugin.crateKey(), PersistentDataType.STRING, (key ? "key:" : "crate:") + type.id());
        item.setItemMeta(meta);
        return item;
    }

    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) { sender.sendMessage("§6/crate give|list|remove|reload|preview|key"); return true; }
        String sub = args[0].toLowerCase();
        if (sub.equals("list")) { sender.sendMessage("§6Crates: §f" + String.join("§7, §f", Arrays.stream(CrateType.values()).map(CrateType::id).toList())); return true; }
        if (sub.equals("reload")) { if (!sender.hasPermission("epiccrates.reload")) return deny(sender); plugin.reloadConfig(); sender.sendMessage("§aKonfiguration neu geladen."); return true; }
        if (sub.equals("remove")) return remove(sender);
        if (sub.equals("preview") && args.length >= 2) { CrateType type = CrateType.fromId(args[1]); if (type == null || !(sender instanceof Player player)) { sender.sendMessage("§cUngültiger Typ oder nur für Spieler."); return true; } new PreviewGUI(plugin).open(player, type); return true; }
        if (sub.equals("give") && args.length >= 3) return give(sender, args, false);
        if (sub.equals("key") && args.length >= 4 && args[1].equalsIgnoreCase("give")) return give(sender, new String[]{"give", args[2], args[3], args.length > 4 ? args[4] : "1"}, true);
        sender.sendMessage("§cVerwendung: /crate give <spieler> <typ> [anzahl]"); return true;
    }

    private boolean give(CommandSender sender, String[] args, boolean key) {
        if (!sender.hasPermission("epiccrates.give")) return deny(sender);
        Player target = Bukkit.getPlayerExact(args[1]); CrateType type = CrateType.fromId(args[2]);
        if (target == null || type == null) { sender.sendMessage("§cSpieler oder Typ nicht gefunden."); return true; }
        int amount; try { amount = args.length > 3 ? Integer.parseInt(args[3]) : 1; } catch (NumberFormatException exception) { amount = 1; }
        target.getInventory().addItem(createItem(plugin, type, key, amount));
        sender.sendMessage(ColorUtil.replace(key ? "&aDu hast &e%amount%x %type%-Key &aan &e%player% &agegeben." : "&aDu hast &e%amount%x %type% &aan &e%player% &agegeben.", "%amount%", String.valueOf(amount), "%type%", type.id(), "%player%", target.getName()));
        return true;
    }

    private boolean remove(CommandSender sender) {
        if (!(sender instanceof Player player) || !sender.hasPermission("epiccrates.admin")) return deny(sender);
        Crate crate = plugin.crates().get(player.getTargetBlockExact(6).getLocation());
        if (crate == null) { sender.sendMessage("§cDu schaust keine Crate an."); return true; }
        plugin.crates().remove(crate.location()); crate.location().getBlock().setType(Material.AIR, false); sender.sendMessage("§aCrate entfernt."); return true;
    }
    private boolean deny(CommandSender sender) { sender.sendMessage("§cDafür fehlt dir die Berechtigung."); return true; }
}
