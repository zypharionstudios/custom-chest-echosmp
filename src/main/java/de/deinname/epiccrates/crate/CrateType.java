package de.deinname.epiccrates.crate;

import org.bukkit.Material;

public enum CrateType {
    COMMON_CRATE("common_crate", Material.IRON_INGOT),
    RARE_CRATE("rare_crate", Material.DIAMOND),
    EPIC_CRATE("epic_crate", Material.NETHERITE_SCRAP),
    LEGENDARY_CRATE("legendary_crate", Material.NETHERITE_INGOT);

    private final String id;
    private final Material previewMaterial;

    CrateType(String id, Material previewMaterial) {
        this.id = id;
        this.previewMaterial = previewMaterial;
    }

    public String id() { return id; }
    public Material previewMaterial() { return previewMaterial; }

    public static CrateType fromId(String value) {
        for (CrateType type : values()) if (type.id.equalsIgnoreCase(value)) return type;
        return null;
    }
}
