package icewizard7.miningServerPlugin.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

public class TabManager {
    private final Plugin plugin;
    private final VanishManager vanishManager;
    private final LuckPermsManager luckPermsManager;
    private BukkitTask tabTask;

    public TabManager(Plugin plugin, VanishManager vanishManager, LuckPermsManager luckPermsManager) {
        this.plugin = plugin;
        this.vanishManager = vanishManager;
        this.luckPermsManager = luckPermsManager;
    }

    public void updateTab(Player player) {
        User user = luckPermsManager.getOrLoadUser(player.getUniqueId());
        Component playerListName = Component.text(player.getName()); // Default

        if (user != null) {
            UUID uuid = player.getUniqueId();
            int weight = luckPermsManager.getWeight(uuid);
            player.setPlayerListOrder(weight);

            // Combine Prefix + Name + Suffix
            playerListName = luckPermsManager.getComponentPrefix(uuid).append(player.name()).append(luckPermsManager.getComponentSuffix(uuid));
        }

        player.playerListName(playerListName);

        // Header
        Component header = Component.text(
                "Welcome to FutureMines", NamedTextColor.GOLD
        ).append(Component.newline());

        // RAM calculation
        Runtime runtime = Runtime.getRuntime();

        long maxMemory = runtime.maxMemory() / 1024 / 1024;     // MB
        long totalMemory = runtime.totalMemory() / 1024 / 1024; // MB
        long freeMemory = runtime.freeMemory() / 1024 / 1024;   // MB
        long usedMemory = totalMemory - freeMemory;

        double usagePercent = (usedMemory / (double) maxMemory) * 100;

        NamedTextColor ramColor;

        if (usagePercent < 50) {
            ramColor = NamedTextColor.GREEN;
        } else if (usagePercent < 75) {
            ramColor = NamedTextColor.GOLD;   // closest to orange in MC
        } else {
            ramColor = NamedTextColor.RED;
        }

        // TPS
        double tps = Bukkit.getServer().getTPS()[0];
        if (tps > 20.0) {
            tps = 20.0; // TPS cannot actually exceed 20 in practice
        }

        NamedTextColor tpsColor;
        if (tps > 18) {
            tpsColor = NamedTextColor.GREEN;
        } else if (tps > 15) {
            tpsColor = NamedTextColor.GOLD;
        } else {
            tpsColor = NamedTextColor.RED;
        }

        // Text
        Component playersOnlineLine = Component.newline().append(Component.text("Players online: " + (Bukkit.getOnlinePlayers().size() - vanishManager.getAmountVanishedPlayers()), NamedTextColor.GRAY)).append(Component.newline());
        Component ipLine = Component.text("IP: futuremines.minekeep.gg", NamedTextColor.GRAY).append(Component.newline());
        Component discordLinkLine = Component.text("Discord: /discord", NamedTextColor.GRAY).append(Component.newline());
        Component ramLine = Component.text("RAM: " + usedMemory + "/" + maxMemory + " MB (", NamedTextColor.GRAY).append(Component.text((int) usagePercent + "%", ramColor)).append(Component.text(")", NamedTextColor.GRAY)).append(Component.newline());
        Component tpsLine = Component.text("TPS: ", NamedTextColor.GRAY).append(Component.text(String.format("%.2f", tps), tpsColor));

        Component footer = playersOnlineLine.append(ipLine).append(discordLinkLine).append(ramLine).append(tpsLine);

        // Send to player
        player.sendPlayerListHeaderAndFooter(header, footer);
    }

    public void joinEvent() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateTab(player);
        }
    }

    public void startTabTask() {
        // TAB Update
        this.tabTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateTab(player);
            }
        }, 0L, 20L * 3); // every 3 seconds
    }

    public void shutdown() {
        if (tabTask != null && !tabTask.isCancelled()) {
            tabTask.cancel();
        }
    }
}
