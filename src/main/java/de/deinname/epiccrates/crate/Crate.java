package de.deinname.epiccrates.crate;

import java.util.UUID;
import org.bukkit.Location;

public record Crate(Location location, CrateType type, UUID owner) {
    public String key() {
        return location.getWorld().getUID() + ":" + location.getBlockX() + ":" + location.getBlockY() + ":" + location.getBlockZ();
    }
}
