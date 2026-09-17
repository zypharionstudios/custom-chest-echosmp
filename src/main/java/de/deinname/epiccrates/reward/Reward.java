package de.deinname.epiccrates.reward;

import org.bukkit.Material;

public record Reward(Material material, int amount, String rarity, double chance) { }
