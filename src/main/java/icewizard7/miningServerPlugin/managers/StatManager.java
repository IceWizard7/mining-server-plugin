package icewizard7.miningServerPlugin.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

public class StatManager {
    private final Plugin plugin;
    private BukkitTask autoSaveTask;
    private BukkitTask scoreboardTask;
    private static final LegacyComponentSerializer LEGACY =
            LegacyComponentSerializer.legacySection();
    private final File file;
    private FileConfiguration data;
    private final Map<UUID, Scoreboard> scoreBoards = new HashMap<>();

    public StatManager(Plugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "player-stats.yml");

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        data = YamlConfiguration.loadConfiguration(file);
    }

    private void save() {
        try { data.save(file); } catch (IOException e) { e.printStackTrace(); }
    }

    private void ensurePlayer(UUID uuid) {
        String path = "stats." + uuid;
        if (!data.contains(path)) {
            data.set(path + ".kills", 0);
            data.set(path + ".deaths", 0);
            data.set(path + ".blocks", 0);
        }
    }

    private int getKills(UUID uuid) {
        return data.getInt("stats." + uuid.toString() + ".kills");
    }

    private int getDeaths(UUID uuid) {
        return data.getInt("stats." + uuid.toString() + ".deaths");
    }

    private int getBlocks(UUID uuid) {
        return data.getInt("stats." + uuid.toString() + ".blocks");
    }

    private int getGlobalBlocks() {
        return data.getInt("globalStats.globalBlocks");
    }

    private void addKill(UUID uuid) {
        ensurePlayer(uuid);
        int kills = getKills(uuid);
        data.set("stats." + uuid.toString() + ".kills", kills + 1);
    }

    private void addDeath(UUID uuid) {
        ensurePlayer(uuid);
        int deaths = getDeaths(uuid);
        data.set("stats." + uuid.toString() + ".deaths", deaths + 1);
    }

    private void addBlock(UUID uuid) {
        ensurePlayer(uuid);
        int blocks = getBlocks(uuid);
        data.set("stats." + uuid.toString() + ".blocks", blocks + 1);
    }

    private void addGlobalBlock() {
        int globalBlocks = getGlobalBlocks();
        data.set("globalStats.globalBlocks", globalBlocks + 1);
    }

    public void killEvent(Player attacker, Player victim) {
        if (attacker != null && attacker != victim) {
            addKill(attacker.getUniqueId());
        }
        addDeath(victim.getUniqueId());
    }

    public void blockMineEvent(Player player) {
        addBlock(player.getUniqueId());
        addGlobalBlock();
    }

    private int getUniqueJoins() {
        File playerDataFolder = new File(Bukkit.getWorldContainer(), "world/playerdata");

        if (!playerDataFolder.exists()) return 0;

        File[] files = playerDataFolder.listFiles((dir, name) -> name.endsWith(".dat"));
        return files == null ? 0 : files.length;
    }

    private String format(long num) {
        if (num >= 1_000_000_000)
            return String.format("%.2fB", num / 1_000_000_000.0);
        else if (num >= 1_000_000)
            return String.format("%.2fM", num / 1_000_000.0);
        else if (num >= 1_000)
            return String.format("%.2fK", num / 1_000.0);
        return String.valueOf(num);
    }

    private void createLine(Scoreboard board, Objective obj, String id, int score) {
        Team team = board.getTeam(id);
        if (team != null) return;

        team = board.registerNewTeam(id);

        // Unique invisible entry using color codes
        String entry = "§" + Integer.toHexString(id.hashCode()).substring(0, 1);
        team.addEntry(entry);

        obj.getScore(entry).setScore(score);
    }

    private void setLine(Scoreboard board, String id, Component text) {
        Team team = board.getTeam(id);
        if (team == null) return;

        team.prefix(text);
    }

    private Component statLine(String name, Object value, Component separator) {
        return Component.text("┃ ", NamedTextColor.BLUE)
                .append(Component.text(name, NamedTextColor.AQUA))
                .append(separator)
                .append(Component.text(String.valueOf(value), NamedTextColor.WHITE));
    }

    public void updateBoard(Player player) {

        Scoreboard board = scoreBoards.computeIfAbsent(player.getUniqueId(),
                k -> Bukkit.getScoreboardManager().getNewScoreboard());

        Objective obj = board.getObjective("stats");
        if (obj == null) {
            obj = board.registerNewObjective("stats", Criteria.DUMMY,
                    Component.text("FutureMines", NamedTextColor.WHITE));
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        }

        // Create lines once
        createLine(board, obj, "l1", 9);
        createLine(board, obj, "l2", 8);
        createLine(board, obj, "l3", 7);
        createLine(board, obj, "l4", 6);
        createLine(board, obj, "l5", 5);
        createLine(board, obj, "l6", 4);
        createLine(board, obj, "l7", 3);
        createLine(board, obj, "l8", 2);
        createLine(board, obj, "l9", 1);
        createLine(board, obj, "l10", 0);

        int kills = getKills(player.getUniqueId());
        int deaths = getDeaths(player.getUniqueId());
        double kdr = deaths == 0 ? kills : (double) kills / deaths;
        int blocks = getBlocks(player.getUniqueId());
        int global = getGlobalBlocks();
        double tps = Math.min(20.0, Bukkit.getServer().getTPS()[0]);
        int uniqueJoins = getUniqueJoins();

        Component separator = Component.text(" » ", NamedTextColor.GRAY);

        setLine(board, "l1", Component.text("Your Stats", NamedTextColor.BLUE, TextDecoration.BOLD));
        setLine(board, "l2", statLine("Kills", kills, separator));
        setLine(board, "l3", statLine("Deaths", deaths, separator));
        setLine(board, "l4", statLine("KDR", String.format("%.2f", kdr), separator));
        setLine(board, "l5", Component.text("Block Stats", NamedTextColor.BLUE, TextDecoration.BOLD));
        setLine(board, "l6", statLine("Mined", format(blocks), separator));
        setLine(board, "l7", statLine("Global Mined", format(global), separator));
        setLine(board, "l8", Component.text("Server Stats", NamedTextColor.BLUE, TextDecoration.BOLD));
        setLine(board, "l9", statLine("TPS", String.format("%.2f", tps), separator));
        setLine(board, "l10", statLine("Unique Joins", format(uniqueJoins), separator));

        player.setScoreboard(board);
    }

    public void startAutoSave() {
        autoSaveTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            save();
        }, 20L * 60, 20L * 60); // every 60s
    }

    public void startScoreboardTask() {
        scoreboardTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateBoard(player);
            }
        }, 0L, 100L); // every 10 seconds
    }

    public void shutdown() {
        if (scoreboardTask != null && !scoreboardTask.isCancelled()) {
            scoreboardTask.cancel();
        }
        if (autoSaveTask != null && !autoSaveTask.isCancelled()) {
            autoSaveTask.cancel();
        }

        // Save one last time before shutting down
        save();
    }

    public boolean hasAlreadyJoined(Player player) {
        File playerDataFolder = new File(Bukkit.getWorldContainer(), "world/playerdata");

        if (!playerDataFolder.exists()) return false;

        // Player files are named with their UUID + ".dat"
        File playerFile = new File(playerDataFolder, player.getUniqueId() + ".dat");
        return playerFile.exists();
    }

    public Map<UUID, String> getTopPlayers(String stat, int limit) {
        if (!stat.equals("kills") && !stat.equals("deaths") && !stat.equals("blocks")) {
            throw new IllegalArgumentException("Invalid stat: " + stat);
        }

        Map<UUID, Integer> allStats = new HashMap<>();

        if (data.contains("stats")) {
            for (String key : data.getConfigurationSection("stats").getKeys(false)) {
                UUID uuid = UUID.fromString(key);
                int value = data.getInt("stats." + key + "." + stat);
                allStats.put(uuid, value);
            }
        }

        // Get all entries as a list
        List<Map.Entry<UUID, Integer>> entries = new ArrayList<>(allStats.entrySet());

        // Sort descending by value
        entries.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        // Take the top 'limit' entries
        List<Map.Entry<UUID, Integer>> topEntries = entries.stream()
                .limit(limit)
                .toList();

        // Put them into a LinkedHashMap to preserve order
        Map<UUID, String> topMap = new LinkedHashMap<>();
        for (Map.Entry<UUID, Integer> entry : topEntries) {
            topMap.put(entry.getKey(), format(entry.getValue()));
        }

        return topMap;
    }

    public void quitEvent(Player player) {
        scoreBoards.remove(player.getUniqueId());
    }
}
