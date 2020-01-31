package com.winthier.dusk;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

final class DuskTask extends BukkitRunnable {
    final DuskPlugin plugin;
    // Params
    final int radius;
    final int blocksPerTick;
    final Player player;
    final World world;
    // State
    final int cx;
    final int cy;
    final int cz;
    int dx;
    int dy;
    int dz;
    int blockCount;

    DuskTask(final DuskPlugin plugin, final Player player,
                    final int radius, final int blocksPerTick) {
        this.plugin = plugin;
        this.player = player;
        this.world = player.getWorld();
        final Location loc = player.getEyeLocation();
        cx = loc.getBlockX();
        cy = loc.getBlockY();
        cz = loc.getBlockZ();
        dx = -radius;
        dy = -radius;
        dz = -radius;
        this.radius = radius;
        this.blocksPerTick = blocksPerTick;
    }

    void start() {
        runTaskTimer(plugin, 1L, 1L);
    }

    void stop() {
        try {
            cancel();
        } catch (IllegalStateException e) { }
        plugin.tasks.remove(player.getUniqueId());
    }

    @Override
    public void run() {
        if (!player.isValid()
            || !player.getWorld().equals(world)) {
            stop();
            return;
        }
        int i = 0;
        for (; dy <= radius; dy++) {
            for (; dz <= radius; dz++) {
                for (; dx <= radius; dx++) {
                    if (i++ > blocksPerTick) return;
                    final int x;
                    final int y;
                    final int z;
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
        player.sendMessage("" + ChatColor.DARK_RED + "[Dusk] "
                           + blockCount + " blocks highlighted within "
                           + radius + " blocks.");
        stop();
    }

    void checkBlock(Block block) {
        if (!world.isChunkLoaded(block.getX() >> 4, block.getZ() >> 4)) return;
        if (block.isEmpty()) return;
        if (block.isLiquid()) return;
        final Material mat = block.getType();
        if (!Tag.VALID_SPAWN.isTagged(mat)) return;
        final Block above = block.getRelative(0, 1, 0);
        final Material aboveMat = above.getType();
        if (!Tag.DOORS.isTagged(aboveMat) && !above.isEmpty()) {
            if (above.isLiquid()) return;
            if (aboveMat.isOccluding()) return;
            if (aboveMat.isSolid()) return;
            switch (aboveMat) {
            case ACTIVATOR_RAIL:
            case DETECTOR_RAIL:
            case POWERED_RAIL:
            case RAIL:
                return;
            default:
                break;
            }
        }
        BlockData bd = block.getBlockData();
        if (bd instanceof Stairs) {
            if (((Stairs) bd).getHalf() != Bisected.Half.TOP) return;
        }
        if (bd instanceof Slab) {
            if (((Slab) bd).getType() == Slab.Type.BOTTOM) return;
        }
        if (bd instanceof org.bukkit.block.data.type.Leaves) return;
        if (bd instanceof org.bukkit.block.data.type.Fence) return;
        final int blockLight = (int) above.getLightFromBlocks();
        if (blockLight > 7) return;
        player.spawnParticle(Particle.BARRIER,
                             block.getLocation().add(0.5, 1.5, 0.5),
                             1,
                             0, 0, 0,
                             0.0);
        blockCount++;
    }
}
