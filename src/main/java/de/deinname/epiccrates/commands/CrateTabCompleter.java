package de.deinname.epiccrates.commands;

import de.deinname.epiccrates.EpicCrates;
import de.deinname.epiccrates.crate.CrateType;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public final class CrateTabCompleter implements TabCompleter {
    public CrateTabCompleter(EpicCrates plugin) { }
    @Override public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return Arrays.asList("give", "list", "remove", "reload", "preview", "key").stream().filter(value -> value.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        if ((args[0].equalsIgnoreCase("give") && args.length == 3) || (args[0].equalsIgnoreCase("preview") && args.length == 2) || (args[0].equalsIgnoreCase("key") && args.length == 4)) return Arrays.stream(CrateType.values()).map(CrateType::id).filter(value -> value.startsWith(args[args.length - 1].toLowerCase())).toList();
        if (args[0].equalsIgnoreCase("key") && args.length == 2) return Collections.singletonList("give");
        return Collections.emptyList();
    }
}
