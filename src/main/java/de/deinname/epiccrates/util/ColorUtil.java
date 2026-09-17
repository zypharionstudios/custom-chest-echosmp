package de.deinname.epiccrates.util;

import org.bukkit.ChatColor;

public final class ColorUtil {
    private ColorUtil() { }

    public static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text == null ? "" : text);
    }

    public static String replace(String text, String... values) {
        String result = text == null ? "" : text;
        for (int index = 0; index + 1 < values.length; index += 2) {
            result = result.replace(values[index], values[index + 1]);
        }
        return color(result);
    }
}
