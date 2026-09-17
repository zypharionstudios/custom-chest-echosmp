package de.deinname.epiccrates.reward;

import de.deinname.epiccrates.EpicCrates;
import de.deinname.epiccrates.crate.CrateType;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public final class RewardManager {
    private final EpicCrates plugin;

    public RewardManager(EpicCrates plugin) { this.plugin = plugin; }

    public List<Reward> getRewards(CrateType type) {
        List<Reward> rewards = new ArrayList<>();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("crates." + type.id() + ".loot");
        if (section == null) return rewards;
        for (String key : section.getKeys(false)) {
            Material material = Material.matchMaterial(section.getString(key + ".material", "STONE"));
            if (material == null) continue;
            rewards.add(new Reward(material, Math.max(1, section.getInt(key + ".amount", 1)),
                    section.getString(key + ".rarity", "COMMON"), Math.max(0, section.getDouble(key + ".chance", 1))));
        }
        return rewards;
    }

    public Reward choose(CrateType type) {
        List<Reward> rewards = getRewards(type);
        if (rewards.isEmpty()) return new Reward(Material.IRON_INGOT, 1, "COMMON", 1);
        double total = rewards.stream().mapToDouble(Reward::chance).sum();
        double roll = ThreadLocalRandom.current().nextDouble(Math.max(total, 1));
        for (Reward reward : rewards) {
            roll -= reward.chance();
            if (roll <= 0) return reward;
        }
        return rewards.get(rewards.size() - 1);
    }

    public ItemStack item(Reward reward) {
        return new ItemStack(reward.material(), reward.amount());
    }
}
