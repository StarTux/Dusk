package com.winthier.dusk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class DuskPlugin extends JavaPlugin implements Listener {
    public final Map<UUID, DuskTask> tasks = new HashMap<>();
    private int blocksPerTick;
    private int radius;
    Metadata meta = new Metadata(this);
    static final String AUDIT = "dusk.audit";

    @Override
    public void onEnable() {
        reloadConfig();
        saveDefaultConfig();
        radius = getConfig().getInt("radius", 32);
        blocksPerTick = getConfig().getInt("blocks-per-tick", 8192);
        getServer().getPluginManager().registerEvents(this, this);
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
        if (args.length == 0) {
            return showDusk(player);
        }
        return onCommand(player, args[0],
                         Arrays.copyOfRange(args, 1, args.length));
    }

    boolean onCommand(Player player, String cmd, String[] args) {
        switch (cmd) {
        case "audit":
            if (args.length != 0) return false;
            if (meta.has(player, AUDIT)) {
                meta.remove(player, AUDIT);
                player.sendMessage(ChatColor.YELLOW + "Audit mode disabled");
            } else {
                meta.set(player, AUDIT, true);
                player.sendMessage(ChatColor.YELLOW + "Audit mode enabled");
            }
            return true;
        default: return false;
        }
    }

    boolean showDusk(Player player) {
        UUID uuid = player.getUniqueId();
        if (tasks.get(uuid) != null) {
            player.sendMessage("" + ChatColor.RED
                               + "[Dusk] Already highlighting blocks for you."
                               + " Please wait a moment.");
            return true;
        }
        DuskTask task = new DuskTask(this, player, radius, blocksPerTick);
        tasks.put(uuid, task);
        task.start();
        player.sendMessage("" + ChatColor.DARK_RED
                           + "[Dusk] Highlighting dark spots with barriers.");
        return true;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;
        if (!meta.has(killer, AUDIT)) return;
        Location origin = event.getEntity().getOrigin();
        if (origin == null) return;
        String name = event.getEntity().getType().name().toLowerCase().replace("_", " ");
        killer.sendMessage(ChatColor.YELLOW + "[Dusk] Audit: " + name + " spawned at "
                           + ChatColor.WHITE + origin.getBlockX() + " " + origin.getBlockY() + " " + origin.getBlockZ() + ".");
    }
}
