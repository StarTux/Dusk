package com.winthier.dusk;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Stairs;
import org.bukkit.material.Step;
import org.bukkit.scheduler.BukkitRunnable;

public class DuskTask extends BukkitRunnable {
    private final DuskPlugin plugin;
    // Params
    private final int radius;
    private final int blocksPerTick;
    private final Player player;
    private final World world;
    private final DuskBlock duskBlock;
    // State
    private final int cx, cy, cz;
    private int dx, dy, dz;
    private int blockCount;

    public DuskTask(DuskPlugin plugin, Player player, DuskBlock duskBlock, int radius, int blocksPerTick) {
        this.plugin = plugin;
        this.player = player;
        this.world = player.getWorld();
        this.duskBlock = duskBlock;
        final Location loc = player.getEyeLocation();
        cx = loc.getBlockX();
        cy = loc.getBlockY();
        cz = loc.getBlockZ();
        dx = dy = dz = -radius;
        this.radius = radius;
        this.blocksPerTick = blocksPerTick;
    }

    public void start() {
        runTaskTimer(plugin, 1L, 1L);
    }

    @Override
    public void run() {
        int i = 0;
        for (; dy <= radius; dy++) {
            for (; dz <= radius; dz++) {
                for (; dx <= radius; dx++) {
                    if (i++ > blocksPerTick) return;
                    final int x, y, z;
                    x = cx + dx;
                    y = cy + dy;
                    z = cz + dz;
                    final Block block = world.getBlockAt(x, y, z);
                    checkBlock(block);
                }
                dx = -radius;
            }
            dz = -radius;
        }
        player.sendMessage("" + ChatColor.AQUA + blockCount + " blocks highlighted within " + radius + " meters.");
        try { cancel(); } catch (IllegalStateException e) {}
        plugin.tasks.remove(player);
    }

    private void checkBlock(Block block) {
        //player.sendMessage("Block " + block.getX() + "," + block.getY() + "," + block.getZ());
        final Material mat = block.getType();
        if (mat == Material.AIR) return;
        final Block above = block.getRelative(0, 1, 0);
        final Material aboveMat = above.getType();
        if (aboveMat.isOccluding() || aboveMat.isSolid()) return;
        switch (mat) {
        case BIRCH_WOOD_STAIRS:
        case BRICK_STAIRS:
        case COBBLESTONE_STAIRS:
        case JUNGLE_WOOD_STAIRS:
        case NETHER_BRICK_STAIRS:
        case QUARTZ_STAIRS:
        case SANDSTONE_STAIRS:
        case SMOOTH_STAIRS:
        case SPRUCE_WOOD_STAIRS:
        case WOOD_STAIRS:
            Stairs stairs = new Stairs(mat, block.getData());
            if (!stairs.isInverted()) return;
        case STEP:
        case WOOD_STEP:
            Step step = new Step(mat, block.getData());
            if (!step.isInverted()) return;
        case SNOW:
            if (block.getData() < 7) return;

        case ACTIVATOR_RAIL:
        case BED_BLOCK:
        case BREWING_STAND:
        case CACTUS:
        case CAKE_BLOCK:
        case CARROT:
        case CHEST:
        case COBBLE_WALL:
        case CROPS:
        case DEAD_BUSH:
        case DETECTOR_RAIL:
        case DIODE_BLOCK_OFF:
        case DIODE_BLOCK_ON:
        case DRAGON_EGG:
        case ENCHANTMENT_TABLE:
        case ENDER_PORTAL_FRAME:
        case FENCE:
        case FENCE_GATE:
        case FLOWER_POT:
        case GLASS:
        case GLOWSTONE:
        case ICE:
        case IRON_DOOR_BLOCK:
        case IRON_FENCE:
        case IRON_PLATE:
        case LADDER:
        case LAVA:
        case LEAVES:
        case LEVER:
        case LONG_GRASS:
        case NETHER_FENCE:
        case NETHER_WARTS:
        case POTATO:
        case POWERED_RAIL:
        case RAILS:
        case REDSTONE_COMPARATOR_OFF:
        case REDSTONE_COMPARATOR_ON:
        case REDSTONE_TORCH_OFF:
        case REDSTONE_TORCH_ON:
        case REDSTONE_WIRE:
        case RED_ROSE:
        case SIGN_POST:
        case SKULL:
        case SOIL:
        case STATIONARY_LAVA:
        case STATIONARY_WATER:
        case STONE_BUTTON:
        case STONE_PLATE:
        case SUGAR_CANE_BLOCK:
        case TRAPPED_CHEST:
        case TRAP_DOOR:
        case TRIPWIRE:
        case TRIPWIRE_HOOK:
        case VINE:
        case WALL_SIGN:
        case WATER:
        case WATER_LILY:
        case WEB:
        case WOODEN_DOOR:
        case WOOD_BUTTON:
        case WOOD_PLATE:
        case YELLOW_FLOWER:

            return;
        default: break;
        }
        int blockLight = (int)above.getLightFromBlocks();
        if (blockLight > 7) return;
        int sunLight = (int)above.getLightFromSky();
        MaterialData data = duskBlock.dayBlock;
        if (sunLight > 7) data = duskBlock.nightBlock;
        player.sendBlockChange(block.getLocation(), data.getItemType(), data.getData());
        blockCount++;
    }
}
