package icewizard7.miningServerPlugin;

import icewizard7.miningServerPlugin.commands.FlyCommand;
import icewizard7.miningServerPlugin.commands.GodCommand;
import icewizard7.miningServerPlugin.commands.InfoCommand;
import icewizard7.miningServerPlugin.commands.VanishCommand;
import icewizard7.miningServerPlugin.events.TabJoinEvent;
import icewizard7.miningServerPlugin.events.VanishJoinEvent;
import icewizard7.miningServerPlugin.events.WelcomeMessageEvent;

import icewizard7.miningServerPlugin.utils.TAB;

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
        getCommand("autocompress").setExecutor(new FlyCommand());
        getCommand("vanish").setExecutor(new VanishCommand(this, vanishedPlayers));

        // Set events
        getServer().getPluginManager().registerEvents(new WelcomeMessageEvent(), this);
        getServer().getPluginManager().registerEvents(new TabJoinEvent(), this);
        getServer().getPluginManager().registerEvents(new VanishJoinEvent(this, vanishedPlayers), this);

        // You could call this on login or periodically
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                TAB.updateTab(player);
            }
        }, 0L, 20L * 1); // every 1 second

        // Print to console
        System.out.println("[MiningServerPlugin] Plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        System.out.println("[MiningServerPlugin] Plugin has been disabled!");
    }
}
