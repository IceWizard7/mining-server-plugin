package icewizard7.miningServerPlugin.classes;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import icewizard7.miningServerPlugin.managers.LuckPermsManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LeaderboardHologram {
    private Hologram hologram;
    private final LuckPermsManager luckPermsManager;

    public LeaderboardHologram(LuckPermsManager luckPermsManager) {
        this.luckPermsManager = luckPermsManager;
    }

    public void spawnLeaderboard(Location location, String name, String stat, Map<UUID, String> topPlayers) {
        // Remove old if exists
        if (hologram != null) {
            hologram.delete();
        }

        // Create a new hologram at the location (not persistent)
        hologram = DHAPI.createHologram(name, location);

        // Build lines
        List<String> lines = new ArrayList<>();
        lines.add("&c&l" + stat.substring(0, 1).toUpperCase() + stat.substring(1) + " Leaderboard");
        lines.add("");

        int rank = 1;
        for (Map.Entry<UUID, String> entry : topPlayers.entrySet()) {
            UUID uuid = entry.getKey();
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            String playerName = player.getName();
            String prefix = luckPermsManager.getStringPrefix(uuid);
            if (prefix == null) prefix = "";
            if (playerName == null) playerName = "Unknown";
            lines.add("&4" + rank + ". &c" + prefix + " " + playerName + "&f (" + entry.getValue() + " " + stat + ")");
            rank++;
        }

        // Set all lines on the hologram (overwrites default)
        DHAPI.setHologramLines(hologram, lines);
        System.out.println("Hologram lines for " + stat + ": " + lines);
    }

    public void updateLeaderboard(String stat, Map<UUID, String> topPlayers) {
        if (hologram == null) return;

        List<String> newLines = new ArrayList<>();
        newLines.add("&c&l" + stat.substring(0, 1).toUpperCase() + stat.substring(1) + " Leaderboard");
        newLines.add("");

        int rank = 1;
        for (Map.Entry<UUID, String> entry : topPlayers.entrySet()) {
            UUID uuid = entry.getKey();
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            String playerName = player.getName();
            String prefix = luckPermsManager.getStringPrefix(uuid);
            if (prefix == null) prefix = "";
            if (playerName == null) playerName = "Unknown";
            newLines.add("&4" + rank + ". &c" + prefix + " " + playerName + "&f (" + entry.getValue() + " " + stat + ")");
            rank++;
        }

        // Replace lines with new content
        DHAPI.setHologramLines(hologram, newLines);
        System.out.println("Hologram lines for " + stat + ": " + newLines);
    }

    public boolean isSpawned() {
        return hologram != null;
    }

    public void delete() {
        if (hologram != null) {
            hologram.delete();
            hologram = null;
        }
    }
}
