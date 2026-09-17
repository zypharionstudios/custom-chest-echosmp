package de.deinname.epiccrates.crate;

import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;

public record Crate(Location location, CrateType type, UUID owner, BlockFace facing) {
    public Crate(Location location, CrateType type, UUID owner) {
        this(location, type, owner, BlockFace.NORTH);
    }

    public String key() {
        return location.getWorld().getUID() + ":" + location.getBlockX() + ":" + location.getBlockY() + ":" + location.getBlockZ();
    }
}
