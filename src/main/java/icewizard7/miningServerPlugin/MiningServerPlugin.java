package icewizard7.miningServerPlugin;

import icewizard7.miningServerPlugin.commands.*;
import icewizard7.miningServerPlugin.events.*;
import icewizard7.miningServerPlugin.utils.*;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;

import net.luckperms.api.LuckPerms;

import java.util.HashSet;
import java.util.Set;

public final class MiningServerPlugin extends JavaPlugin {
    // Set of vanished Players
    private final Set<Player> vanishedPlayers = new HashSet<>();

    @Override
    public void onEnable() {
        // Save config.yml to plugins folder
        saveDefaultConfig();

        // Load LuckPerms API
        LuckPerms luckPerms = getServer().getServicesManager().load(LuckPerms.class);

        // Load commands
        getCommand("info").setExecutor(new InfoCommand());
        getCommand("god").setExecutor(new GodCommand());
        getCommand("invsee").setExecutor(new InvseeCommand());
        getCommand("fly").setExecutor(new FlyCommand());
        getCommand("autocompress").setExecutor(new AutoCompressCommand(this));
        getCommand("warp").setExecutor(new WarpCommand(this));
        getCommand("spawn").setExecutor(new SpawnCommand(this));
        getCommand("vanish").setExecutor(new VanishCommand(this, vanishedPlayers));

        // Set events
        getServer().getPluginManager().registerEvents(new ChatEvent(luckPerms), this);
        getServer().getPluginManager().registerEvents(new WelcomeMessageEvent(), this);
        getServer().getPluginManager().registerEvents(new TabJoinEvent(), this);
        getServer().getPluginManager().registerEvents(new VanishEvent(this, vanishedPlayers), this);

        // TAB Update
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
