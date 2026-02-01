package icewizard7.miningServerPlugin.managers;

import icewizard7.miningServerPlugin.classes.LeaderboardHologram;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LeaderboardManager {
    private final Plugin plugin;
    private final FileConfiguration config;
    private final StatManager statManager;

    private final Map<String, LeaderboardHologram> holograms = new HashMap<>();
    private final Map<String, BukkitTask> tasks = new HashMap<>();
    private final long refreshTicks = 20L * 60; // 60 seconds

    public LeaderboardManager(Plugin plugin, StatManager statManager) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.statManager = statManager;
    }

    private Location getLocation(String hologramName) {
        String worldName = config.getString("holograms." + hologramName + ".world");
        World world = Bukkit.getWorld(worldName);
        double x = config.getDouble("holograms." + hologramName + ".x");
        double y = config.getDouble("holograms." + hologramName + ".y");
        double z = config.getDouble("holograms." + hologramName + ".z");
        float yaw = (float) config.getDouble("holograms." + hologramName + ".yaw");
        float pitch = (float) config.getDouble("holograms." + hologramName + ".pitch");

        return new Location(world, x + 0.5, y, z + 0.5, yaw, pitch);
    }

    public void startLeaderboard(String stat) {
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Map<UUID, String> top = statManager.getTopPlayers(stat, 10);
            LeaderboardHologram holo = holograms.get(stat);
            if (holo != null && holo.isSpawned()) {  // add isSpawned() check
                holo.updateLeaderboard(stat, top);
            } else {
                holo = new LeaderboardHologram();
                holo.spawnLeaderboard(getLocation(stat), stat + "Leaderboard", stat, top);
                holograms.put(stat, holo);
            }
        }, 0L, refreshTicks);
        tasks.put(stat, task);
    }


    public void startAllLeaderboards() {
        startLeaderboard("kills");
        startLeaderboard("deaths");
        startLeaderboard("blocks");
    }

    public void shutdown() {
        tasks.values().forEach(task -> {
            if (task != null && !task.isCancelled()) task.cancel();
        });
        holograms.values().forEach(holo -> {
            if (holo != null) holo.delete();
        });
        tasks.clear();
        holograms.clear();
    }
}
