package de.deinname.epiccrates.commands;

import de.deinname.epiccrates.EpicCrates;
import de.deinname.epiccrates.crate.Crate;
import de.deinname.epiccrates.crate.CrateType;
import de.deinname.epiccrates.gui.PreviewGUI;
import de.deinname.epiccrates.util.ColorUtil;
import java.util.Arrays;
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
        if (sender instanceof Player player && !player.isOp()) {
            sender.sendMessage("§cAlle /crate-Befehle sind nur für Operatoren verfügbar.");
            return true;
        }
        if (args.length == 0) { sendHelp(sender); return true; }
        String sub = args[0].toLowerCase();
        if (sub.equals("list")) { sender.sendMessage("§6Crates: §f" + String.join("§7, §f", Arrays.stream(CrateType.values()).map(CrateType::id).toList())); return true; }
        if (sub.equals("reload")) { if (!sender.hasPermission("epiccrates.reload")) return deny(sender); plugin.reloadConfig(); sender.sendMessage("§aKonfiguration neu geladen."); return true; }
        if (sub.equals("remove")) return remove(sender);
        if (sub.equals("preview") && args.length >= 2) { CrateType type = CrateType.fromId(args[1]); if (type == null || !(sender instanceof Player player)) { sender.sendMessage("§cUngültiger Typ oder nur für Spieler."); return true; } new PreviewGUI(plugin).open(player, type); return true; }
        if (sub.equals("give")) return give(sender, args, false);
        if (sub.equals("key") && args.length >= 2 && args[1].equalsIgnoreCase("give")) return give(sender, args, true);
        sendHelp(sender); return true;
    }

    private boolean give(CommandSender sender, String[] args, boolean key) {
        if (!sender.hasPermission("epiccrates.give")) return deny(sender);
        int typeIndex;
        int amountIndex;
        Player target;
        if (key) {
            if (args.length < 3) { sendKeyUsage(sender); return true; }
            boolean shortForm = CrateType.fromId(args[2]) != null;
            typeIndex = shortForm ? 2 : 3;
            amountIndex = typeIndex + 1;
            target = shortForm ? sender instanceof Player player ? player : null : Bukkit.getPlayerExact(args[2]);
        } else {
            if (args.length < 2) { sendGiveUsage(sender); return true; }
            boolean shortForm = CrateType.fromId(args[1]) != null;
            typeIndex = shortForm ? 1 : 2;
            amountIndex = typeIndex + 1;
            target = shortForm ? sender instanceof Player player ? player : null : Bukkit.getPlayerExact(args[1]);
        }
        CrateType type = args.length > typeIndex ? CrateType.fromId(args[typeIndex]) : null;
        if (target == null || type == null) { sender.sendMessage("§cSpieler oder Crate-Typ nicht gefunden. Nutze /crate list."); return true; }
        int amount; try { amount = args.length > amountIndex ? Integer.parseInt(args[amountIndex]) : 1; } catch (NumberFormatException exception) { amount = 1; }
        if (amount < 1) { sender.sendMessage("§cDie Anzahl muss mindestens 1 sein."); return true; }
        target.getInventory().addItem(createItem(plugin, type, key, amount));
        sender.sendMessage(ColorUtil.replace(key ? "&aDu hast &e%amount%x %type%-Key &aan &e%player% &agegeben." : "&aDu hast &e%amount%x %type% &aan &e%player% &agegeben.", "%amount%", String.valueOf(amount), "%type%", type.id(), "%player%", target.getName()));
        return true;
    }

    private boolean remove(CommandSender sender) {
        if (!(sender instanceof Player player) || !sender.hasPermission("epiccrates.admin")) return deny(sender);
        org.bukkit.block.Block targetBlock = player.getTargetBlockExact(6);
        if (targetBlock == null) { sender.sendMessage("§cDu schaust keinen Block an."); return true; }
        Crate crate = plugin.crates().get(targetBlock.getLocation());
        if (crate == null) { sender.sendMessage("§cDu schaust keine Crate an."); return true; }
        plugin.crates().remove(crate.location()); crate.location().getBlock().setType(Material.AIR, false); sender.sendMessage("§aCrate entfernt."); return true;
    }
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6EpicCrates Befehle:");
        sender.sendMessage("§e/crate give <spieler> <typ> [anzahl] §7- Crate vergeben");
        sender.sendMessage("§e/crate give <typ> [anzahl] §7- Crate an dich selbst");
        sender.sendMessage("§e/crate key give <spieler> <typ> [anzahl] §7- Key vergeben");
        sender.sendMessage("§e/crate key give <typ> [anzahl] §7- Key an dich selbst");
        sender.sendMessage("§e/crate list §7- Crate-Typen anzeigen");
        sender.sendMessage("§e/crate preview <typ> §7- Loot-Vorschau öffnen");
        sender.sendMessage("§e/crate remove §7- angesehene Crate entfernen");
        sender.sendMessage("§e/crate reload §7- Konfiguration neu laden");
    }
    private void sendGiveUsage(CommandSender sender) {
        sender.sendMessage("§cVerwendung: /crate give <spieler> <typ> [anzahl]");
        sender.sendMessage("§7Kurzform: /crate give <typ> [anzahl]");
    }
    private void sendKeyUsage(CommandSender sender) {
        sender.sendMessage("§cVerwendung: /crate key give <spieler> <typ> [anzahl]");
        sender.sendMessage("§7Kurzform: /crate key give <typ> [anzahl]");
    }
    private boolean deny(CommandSender sender) { sender.sendMessage("§cDafür fehlt dir die Berechtigung."); return true; }
}
