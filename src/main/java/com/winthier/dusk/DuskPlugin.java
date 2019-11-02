package com.winthier.dusk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class DuskPlugin extends JavaPlugin {
    public final Map<UUID, DuskTask> tasks = new HashMap<>();
    private int blocksPerTick;
    private int radius;

    @Override
    public void onEnable() {
        reloadConfig();
        radius = getConfig().getInt("radius", 32);
        blocksPerTick = getConfig().getInt("blocks-per-tick", 8192);
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    @Override
    public void onDisable() {
        for (DuskTask task : new ArrayList<>(tasks.values())) {
            task.stop();
        }
        tasks.clear();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {
        Player player = null;
        if (sender instanceof Player) player = (Player) sender;
        if (player == null) {
            sender.sendMessage("Player expected");
            return true;
        }
        if (args.length != 0) return false;
        if (tasks.get(player.getUniqueId()) != null) {
            player.sendMessage("" + ChatColor.RED
                               + "[Dusk] Already highlighting blocks for you."
                               + " Please wait a moment.");
            return true;
        }
        showDusk(player);
        player.sendMessage("" + ChatColor.DARK_RED
                           + "[Dusk] Highlighting dark spots with barriers.");
        return true;
    }

    void showDusk(Player player) {
        DuskTask task = new DuskTask(this, player, radius, blocksPerTick);
        tasks.put(player.getUniqueId(), task);
        task.start();
    }
}
