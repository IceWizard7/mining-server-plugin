package icewizard7.miningServerPlugin;

import icewizard7.miningServerPlugin.commands.FlyCommand;
import icewizard7.miningServerPlugin.commands.GodCommand;
import icewizard7.miningServerPlugin.commands.InfoCommand;
import icewizard7.miningServerPlugin.commands.VanishCommand;
import icewizard7.miningServerPlugin.events.TabJoinEvent;
import icewizard7.miningServerPlugin.events.VanishJoinEvent;
import icewizard7.miningServerPlugin.events.WelcomeMessageEvent;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;

import java.util.HashSet;
import java.util.Set;

public final class MiningServerPlugin extends JavaPlugin {
    private final Set<Player> vanishedPlayers = new HashSet<>();

    @Override
    public void onEnable() {
        // Load commands
        getCommand("info").setExecutor(new InfoCommand());
        getCommand("god").setExecutor(new GodCommand());
        getCommand("fly").setExecutor(new FlyCommand());
        getCommand("vanish").setExecutor(new VanishCommand(this, vanishedPlayers));

        // Set events
        getServer().getPluginManager().registerEvents(new WelcomeMessageEvent(), this);
        getServer().getPluginManager().registerEvents(new TabJoinEvent(this), this);
        getServer().getPluginManager().registerEvents(new VanishJoinEvent(this, vanishedPlayers), this);

        // You could call this on login or periodically
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateTab(player);
            }
        }, 0L, 20L * 1); // every 1 second

        // Print to console
        System.out.println("[MiningServerPlugin] Plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        System.out.println("[MiningServerPlugin] Plugin has been disabled!");
    }

    public void updateTab(Player player) {

        // Header
        Component header = Component.text(
                "Welcome to MyServer", NamedTextColor.GOLD
                ).append(Component.newline());

        // Ram calculation
        Runtime runtime = Runtime.getRuntime();

        long maxMemory = runtime.maxMemory() / 1024 / 1024;     // MB
        long totalMemory = runtime.totalMemory() / 1024 / 1024; // MB
        long freeMemory = runtime.freeMemory() / 1024 / 1024;   // MB
        long usedMemory = totalMemory - freeMemory;

        double usagePercent = (usedMemory / (double) maxMemory) * 100;

        // Colour based on usage
        NamedTextColor ramColor;

        if (usagePercent < 50) {
            ramColor = NamedTextColor.GREEN;
        } else if (usagePercent < 75) {
            ramColor = NamedTextColor.GOLD;   // closest to orange in MC
        } else {
            ramColor = NamedTextColor.RED;
        }

        // Footer
        Component footer = Component.text(
                "Players online: " + Bukkit.getOnlinePlayers().size(), NamedTextColor.GRAY)
                .append(Component.newline())
                .append(Component.newline())
                .append(Component.text(
                        "RAM: " + usedMemory + "/" + maxMemory + " MB (" + (int) usagePercent + "%)",
                        ramColor
                ));

        // Send to player
        player.sendPlayerListHeaderAndFooter(header, footer);
    }
}
