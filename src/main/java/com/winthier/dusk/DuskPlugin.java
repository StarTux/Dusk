package com.winthier.dusk;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class DuskPlugin extends JavaPlugin {
    public final Map<Player, DuskTask> tasks = new HashMap<Player, DuskTask>();
    private int blocksPerTick = 1024;
    private int radius = 32;

    @Override
    public void onEnable() {
        reloadConfig();
        radius = getConfig().getInt("radius", 16);
        blocksPerTick = getConfig().getInt("blocks-per-tick", 1024);
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    @Override
    public void onDisable() {
        for (DuskTask task : tasks.values()) {
            try { task.cancel(); } catch (IllegalStateException e) {}
        }
        tasks.clear();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String args[]) {
        Player player = null;
        if (sender instanceof Player) player = (Player)sender;
        if (player == null) {
            sender.sendMessage("Player expected");
            return true;
        }
        if (args.length > 1) return false;
        if (tasks.get(player) != null) {
            player.sendMessage("" + ChatColor.RED + "Already highlighting blocks for you. Please wait a moment.");
            return true;
        }
        DuskBlock duskBlock = DuskBlock.WOOL;
        if (args.length == 1) {
            duskBlock = DuskBlock.fromString(args[0]);
            if (duskBlock == null) return false;
        }
        showDusk(player, duskBlock);
        player.sendMessage("" + ChatColor.AQUA + "Highlighting dark spots with " + duskBlock.title + ".");
        player.sendMessage("" + duskBlock.nightColor + "[Not safe only at night]" + ChatColor.AQUA + " " + duskBlock.dayColor + "[Not safe any time]" + ChatColor.RESET + ".");
        return true;
    }

    public void showDusk(Player player, DuskBlock duskBlock) {
        DuskTask task = new DuskTask(this, player, duskBlock, radius, blocksPerTick);
        tasks.put(player, task);
        task.start();
    }
}
