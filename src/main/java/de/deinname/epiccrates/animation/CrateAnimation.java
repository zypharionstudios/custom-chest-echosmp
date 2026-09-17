package de.deinname.epiccrates.animation;

import de.deinname.epiccrates.EpicCrates;
import de.deinname.epiccrates.crate.Crate;
import de.deinname.epiccrates.reward.Reward;
import de.deinname.epiccrates.util.ColorUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.Color;
import org.bukkit.block.Chest;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public final class CrateAnimation {
    private final EpicCrates plugin;
    private final Map<String, UUID> active = new HashMap<>();
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<String, BukkitTask> tasks = new HashMap<>();

    public CrateAnimation(EpicCrates plugin) { this.plugin = plugin; }
    public boolean isActive(Crate crate) { return active.containsKey(crate.key()); }
    public long cooldown(UUID player) { return Math.max(0, cooldowns.getOrDefault(player, 0L) - System.currentTimeMillis()); }

    public boolean start(Player player, Crate crate) {
        if (isActive(crate) || cooldown(player.getUniqueId()) > 0) return false;
        active.put(crate.key(), player.getUniqueId());
        setChestOpen(crate, true);
        crate.location().getBlock().setType(org.bukkit.Material.AIR, false);
        player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.BLINDNESS, 10, 0));
        player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SLOW, 10, 10));
        player.sendTitle("§6§l✦ CRATE OPENING ✦", "", 0, 10, 5);
        nearby(crate.location(), 10, p -> p.playSound(crate.location(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.25f, 1f));
        Reward reward = plugin.rewards().choose(crate.type());
        BukkitTask task = new BukkitRunnable() {
            int tick;
            BlockDisplay flyingCrate;
            ItemDisplay display;
            @Override public void run() {
                if (!player.isOnline()) { finish(crate, player); cancel(); return; }
                Location center = crate.location().clone().add(0.5, 1.0, 0.5);
                World world = center.getWorld();
                if (tick < 20) {
                    world.spawnParticle(Particle.CLOUD, center, 4, .25, .15, .25, .02);
                    ParticleRunnable.circle(world, center, 0.7, tick / 20.0, Particle.SMOKE_NORMAL, 12);
                } else if (tick < 60) {
                    if (flyingCrate == null) {
                        flyingCrate = (BlockDisplay) world.spawnEntity(center, EntityType.BLOCK_DISPLAY);
                        flyingCrate.setBlock(org.bukkit.Material.CHEST.createBlockData());
                        flyingCrate.setGlowing(true);
                    }
                    Location flight = center.clone().add(Math.sin(tick * 0.32) * 0.45, (tick - 20) / 18.0, Math.cos(tick * 0.27) * 0.45);
                    flyingCrate.teleport(flight);
                    flyingCrate.setRotation(tick * 18, (float) Math.sin(tick * 0.2) * 15);
                    ParticleRunnable.circle(world, center, 1.1, tick / 20.0, Particle.ENCHANTMENT_TABLE, 18);
                    ParticleRunnable.circle(world, center, 1.45, tick / 15.0, Particle.END_ROD, 14);
                    world.spawnParticle(Particle.FLAME, center.clone().add(0, tick / 25.0, 0), 3, .2, .1, .2, .01);
                    world.spawnParticle(Particle.SOUL_FIRE_FLAME, center.clone().add(0, 1.2, 0), 2, .35, .2, .35, .01);
                    if (tick == 20) nearby(crate.location(), 30, p -> p.playSound(crate.location(), Sound.BLOCK_CHEST_OPEN, .5f, .5f));
                } else if (tick == 60) {
                    if (flyingCrate != null) { flyingCrate.remove(); flyingCrate = null; }
                    world.spawnParticle(Particle.EXPLOSION_HUGE, center, 1);
                    world.spawnParticle(Particle.EXPLOSION_LARGE, center, 8, .7, .7, .7, .1);
                    world.spawnParticle(Particle.CLOUD, center, 40, .9, .6, .9, .08);
                    nearby(crate.location(), 30, p -> { p.playSound(crate.location(), Sound.ENTITY_GENERIC_EXPLODE, .7f, 1.1f); p.playSound(crate.location(), Sound.BLOCK_BEACON_ACTIVATE, .6f, 1f); });
                    display = (ItemDisplay) world.spawnEntity(center.clone().add(0, .4, 0), EntityType.ITEM_DISPLAY);
                    display.setItemStack(plugin.rewards().item(reward)); display.setBillboard(Billboard.CENTER); display.setGlowing(true);
                } else if (tick < 140) {
                    ParticleRunnable.circle(world, center.clone().add(0, 1.5, 0), .8, 1, Particle.TOTEM, 12);
                    ParticleRunnable.circle(world, center.clone().add(0, 1.5, 0), 1.15, 1.5, Particle.FIREWORKS_SPARK, 16);
                    if (tick % 8 == 0) world.spawnParticle(Particle.VILLAGER_HAPPY, center.clone().add(0, 2.2, 0), 8, .35, .3, .35, .01);
                    if (display != null) display.setRotation(tick * 8, 0);
                    if (tick == 100) reveal(player, reward);
                } else if (tick < 165 && display != null) {
                    Location from = center.clone().add(0, 2, 0);
                    display.teleport(from.add(player.getLocation().toVector().subtract(from.toVector()).multiply((tick - 140) / 25.0)));
                } else {
                    if (flyingCrate != null) flyingCrate.remove();
                    if (display != null) display.remove();
                    ItemStack item = plugin.rewards().item(reward);
                    player.getInventory().addItem(item);
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 1);
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                    finish(crate, player); cancel();
                }
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        tasks.put(crate.key(), task);
        return true;
    }

    private void reveal(Player player, Reward reward) {
        String rarity = reward.rarity().toUpperCase();
        String color = rarity.equals("LEGENDARY") ? "§d§l★ LEGENDARY ★" : rarity.equals("RARE") ? "§b§l✦ RARE ✦" : rarity.equals("UNCOMMON") ? "§e§lUncommon" : "§fCommon";
        player.sendTitle(color, "§f" + reward.material().name() + " x" + reward.amount(), 5, 45, 10);
        player.playSound(player.getLocation(), rarity.equals("LEGENDARY") ? Sound.ENTITY_ENDER_DRAGON_GROWL : Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        player.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, player.getLocation().add(0, 1, 0), rarity.equals("LEGENDARY") ? 80 : 35, .5, .7, .5, .08);
    }

    private void nearby(Location location, double radius, java.util.function.Consumer<Player> action) { for (Player player : location.getWorld().getPlayers()) if (player.getLocation().distanceSquared(location) <= radius * radius) action.accept(player); }
    private void finish(Crate crate, Player player) { setChestOpen(crate, false); plugin.crates().restoreBlock(crate); active.remove(crate.key()); tasks.remove(crate.key()); cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + plugin.getConfig().getLong("settings.cooldown-seconds", 20) * 1000L); }
    private void setChestOpen(Crate crate, boolean open) {
        if (crate.location().getBlock().getState() instanceof Chest chest) {
            chest.open();
            if (!open) chest.close();
            chest.update(true, false);
        }
    }
    public void cancelAll() { tasks.values().forEach(BukkitTask::cancel); tasks.clear(); active.clear(); }
}
