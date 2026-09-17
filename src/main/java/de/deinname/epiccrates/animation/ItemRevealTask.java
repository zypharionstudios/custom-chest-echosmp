package de.deinname.epiccrates.animation;

import de.deinname.epiccrates.reward.Reward;
import org.bukkit.Location;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public final class ItemRevealTask extends BukkitRunnable {
    private final ItemDisplay display;
    private final Location start;
    private final Location target;
    private int tick;

    public ItemRevealTask(ItemDisplay display, Location start, Location target) {
        this.display = display; this.start = start; this.target = target;
    }

    @Override public void run() {
        if (!display.isValid() || tick > 50) { cancel(); return; }
        double progress = Math.min(1, tick / 50.0);
        double arc = Math.sin(progress * Math.PI) * 2.5;
        Location next = start.clone().multiply(1 - progress).add(target.clone().multiply(progress));
        next.add(0, arc, 0);
        display.teleport(next);
        display.setRotation(tick * 14, 0);
        tick++;
    }
}
