package com.winthier.dusk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;
import static net.kyori.adventure.text.format.NamedTextColor.*;

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
                player.sendMessage(text("Audit mode disabled", YELLOW));
            } else {
                meta.set(player, AUDIT, true);
                player.sendMessage(text("Audit mode enabled", YELLOW));
            }
            return true;
        default: return false;
        }
    }

    boolean showDusk(Player player) {
        UUID uuid = player.getUniqueId();
        if (tasks.get(uuid) != null) {
            player.sendMessage(text("[Dusk] Already highlighting blocks for you. Please wait a moment.", RED));
            return true;
        }
        DuskTask task = new DuskTask(this, player, radius, blocksPerTick);
        tasks.put(uuid, task);
        task.start();
        player.sendMessage(text("[Dusk] Highlighting dark spots with barriers", DARK_RED));
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
        killer.sendMessage(textOfChildren(text("[Dusk] Audit: " + name + " spawned at ", YELLOW),
                                          text(origin.getBlockX() + " " + origin.getBlockY() + " " + origin.getBlockZ() + ".")));
    }
}
