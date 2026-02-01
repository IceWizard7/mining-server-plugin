package icewizard7.miningServerPlugin.classes;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LeaderboardHologram {
    private Hologram hologram;

    public void spawnLeaderboard(Location location, String name, String stat, Map<UUID, Integer> topPlayers) {
        // Remove old if exists
        if (hologram != null) {
            hologram.delete();
        }

        // Create a new hologram at the location (not persistent)
        hologram = DHAPI.createHologram(name, location);

        // Build lines
        List<String> lines = new ArrayList<>();
        lines.add(stat.substring(0, 1).toUpperCase() + stat.substring(1) + " Leaderboard");

        int rank = 1;
        for (Map.Entry<UUID, Integer> entry : topPlayers.entrySet()) {
            String playerName = Bukkit.getOfflinePlayer(entry.getKey()).getName();
            if (playerName == null) playerName = "Unknown";
            lines.add(rank + ". " + playerName + " » " + entry.getValue());
            rank++;
        }

        // Set all lines on the hologram (overwrites default)
        DHAPI.setHologramLines(hologram, lines);
        System.out.println("Hologram lines for " + stat + ": " + lines);
    }

    public void updateLeaderboard(String stat, Map<UUID, Integer> topPlayers) {
        if (hologram == null) return;

        List<String> newLines = new ArrayList<>();
        newLines.add(stat.substring(0, 1).toUpperCase() + stat.substring(1) + " Leaderboard");

        int rank = 1;
        for (Map.Entry<UUID, Integer> entry : topPlayers.entrySet()) {
            String playerName = Bukkit.getOfflinePlayer(entry.getKey()).getName();
            if (playerName == null) playerName = "Unknown";
            newLines.add(rank + ". " + playerName + " » " + entry.getValue());
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
