package de.deinname.epiccrates.animation;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;

public final class ParticleRunnable {
    private ParticleRunnable() { }
    public static void circle(World world, Location center, double radius, double height, Particle particle, int points) {
        for (int index = 0; index < points; index++) {
            double angle = (Math.PI * 2 * index / points) + height;
            Location point = center.clone().add(Math.cos(angle) * radius, height * 0.35, Math.sin(angle) * radius);
            world.spawnParticle(particle, point, 1, 0, 0.02, 0, 0);
        }
    }
}
