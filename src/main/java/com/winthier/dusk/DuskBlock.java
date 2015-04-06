package com.winthier.dusk;

import org.bukkit.material.Wool;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.material.MaterialData;
import org.bukkit.Material;

public enum DuskBlock {
    WOOL("wool", new Wool(DyeColor.YELLOW), new Wool(DyeColor.RED), ChatColor.YELLOW, ChatColor.RED),
    SNOW("snow", new MaterialData(Material.SNOW_BLOCK), new MaterialData(Material.OBSIDIAN), ChatColor.WHITE, ChatColor.BLACK);
    ;

    public final String title;
    public final MaterialData nightBlock, dayBlock;
    public final ChatColor nightColor, dayColor;

    DuskBlock(String title, MaterialData nightBlock, MaterialData dayBlock, ChatColor nightColor, ChatColor dayColor) {
        this.title = title;
        this.nightBlock = nightBlock;
        this.dayBlock = dayBlock;
        this.nightColor = nightColor;
        this.dayColor = dayColor;
    }

    public static DuskBlock fromString(String string) {
        try {
            return Enum.valueOf(DuskBlock.class, string.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
