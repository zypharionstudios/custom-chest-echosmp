package de.deinname.epiccrates.crate;

import de.deinname.epiccrates.EpicCrates;
import java.io.File;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.configuration.file.YamlConfiguration;

public final class CrateManager {
    private final EpicCrates plugin;
    private final Map<String, Crate> crates = new LinkedHashMap<>();
    private final File file;
    private YamlConfiguration data;

    public CrateManager(EpicCrates plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "crates.yml");
    }

    public void load() {
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        data = YamlConfiguration.loadConfiguration(file);
        crates.clear();
        for (String key : data.getKeys(false)) {
            String worldName = data.getString(key + ".world");
            World world = Bukkit.getWorld(worldName == null ? "" : worldName);
            CrateType type = CrateType.fromId(data.getString(key + ".type", ""));
            if (world == null || type == null) continue;
            Location location = new Location(world, data.getInt(key + ".x"), data.getInt(key + ".y"), data.getInt(key + ".z"));
            String owner = data.getString(key + ".owner");
            Crate crate = new Crate(location, type, owner == null ? null : UUID.fromString(owner));
            crates.put(crate.key(), crate);
            markBlock(crate);
            spawnLabel(crate);
        }
    }

    public void save() {
        data = new YamlConfiguration();
        for (Crate crate : crates.values()) {
            String path = crate.key();
            data.set(path + ".world", crate.location().getWorld().getName());
            data.set(path + ".x", crate.location().getBlockX());
            data.set(path + ".y", crate.location().getBlockY());
            data.set(path + ".z", crate.location().getBlockZ());
            data.set(path + ".type", crate.type().id());
            data.set(path + ".owner", crate.owner() == null ? null : crate.owner().toString());
        }
        try { data.save(file); } catch (Exception exception) { plugin.getLogger().severe("Crates konnten nicht gespeichert werden: " + exception.getMessage()); }
    }

    public Crate create(Location location, CrateType type, UUID owner) {
        Block block = location.getBlock();
        block.setType(Material.CHEST, false);
        Crate crate = new Crate(block.getLocation(), type, owner);
        crates.put(crate.key(), crate);
        markBlock(crate);
        spawnLabel(crate);
        save();
        return crate;
    }

    private void markBlock(Crate crate) {
        if (!(crate.location().getBlock().getState() instanceof TileState state)) return;
        state.getPersistentDataContainer().set(plugin.crateKey(), PersistentDataType.STRING, crate.type().id());
        state.update(true, false);
    }

    /** Zeigt den Namen der Crate dauerhaft über dem Block an. */
    private void spawnLabel(Crate crate) {
        removeLabel(crate.location());
        String configuredName = plugin.getConfig().getString("crates." + crate.type().id() + ".display-name", crate.type().id());
        ArmorStand label = (ArmorStand) crate.location().getWorld().spawnEntity(crate.location().clone().add(0.5, 1.35, 0.5), EntityType.ARMOR_STAND);
        label.setInvisible(true);
        label.setMarker(true);
        label.setGravity(false);
        label.setInvulnerable(true);
        label.setPersistent(true);
        label.setCustomName("§6✦ " + de.deinname.epiccrates.util.ColorUtil.color(configuredName) + " §6✦");
        label.setCustomNameVisible(true);
        label.getPersistentDataContainer().set(plugin.crateLabelKey(), PersistentDataType.STRING, crate.key());
    }

    private void removeLabel(Location location) {
        for (Entity entity : location.getWorld().getNearbyEntities(location.clone().add(0.5, 1.35, 0.5), 1.2, 2.0, 1.2)) {
            if (!(entity instanceof ArmorStand)) continue;
            String key = entity.getPersistentDataContainer().get(plugin.crateLabelKey(), PersistentDataType.STRING);
            if (key != null && key.equals(key(location))) entity.remove();
        }
    }

    public Crate get(Location location) { return crates.get(key(location)); }
    public Collection<Crate> all() { return crates.values(); }
    public Crate remove(Location location) { Crate crate = crates.remove(key(location)); if (crate != null) { removeLabel(location); save(); } return crate; }
    public void restoreBlock(Crate crate) { crate.location().getBlock().setType(Material.CHEST, false); markBlock(crate); }
    public String key(Location location) { return location.getWorld().getUID() + ":" + location.getBlockX() + ":" + location.getBlockY() + ":" + location.getBlockZ(); }
}
