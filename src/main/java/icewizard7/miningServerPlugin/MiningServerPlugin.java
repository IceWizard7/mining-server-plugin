package icewizard7.miningServerPlugin;

import icewizard7.miningServerPlugin.commands.*;
import icewizard7.miningServerPlugin.events.*;
import icewizard7.miningServerPlugin.utils.*;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;

import net.luckperms.api.LuckPerms;

import java.util.List;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;

public final class MiningServerPlugin extends JavaPlugin {
    // Set of vanished Players
    private final Set<UUID> vanishedPlayers = new HashSet<>();

    // TAB
    private TAB tab;

    @Override
    public void onEnable() {
        // Save config.yml to plugins folder
        saveDefaultConfig();

        // Load LuckPerms API
        LuckPerms luckPerms = getServer().getServicesManager().load(LuckPerms.class);

        // Initialize TAB
        this.tab = new TAB(vanishedPlayers);

        // Commands + events
        InfoCommand infoCommand = new InfoCommand();
        GodCommand godCommand = new GodCommand();
        InvseeCommand invseeCommand = new InvseeCommand();
        FlyCommand flyCommand = new FlyCommand();
        AutoCompressCommand autoCompressCommand = new AutoCompressCommand(this);
        WarpCommand warpCommand = new WarpCommand(this);
        SpawnCommand spawnCommand = new SpawnCommand(this);
        VanishCommand vanishCommand = new VanishCommand(this, vanishedPlayers);
        VoucherCommand voucherCommand = new VoucherCommand(luckPerms, this);
        Listener chatEvent = new ChatEvent(luckPerms);
        Listener welcomeEvent = new WelcomeMessageEvent();
        Listener tabJoinEvent = new TabJoinEvent(tab);
        Listener vanishEvent = new VanishEvent(this, vanishedPlayers);
        Listener telepathyEvent = new TelepathyEvent(this, autoCompressCommand);
        Listener spawnPointEvent = new SpawnPointEvent(this);
        Listener voucherUseEvent = new VoucherUseEvent(luckPerms, voucherCommand.getVoucherKey());

        getCommand("info").setExecutor(infoCommand);
        getCommand("god").setExecutor(godCommand);
        getCommand("invsee").setExecutor(invseeCommand);
        getCommand("fly").setExecutor(flyCommand);
        getCommand("autocompress").setExecutor(autoCompressCommand);
        getCommand("warp").setExecutor(warpCommand);
        getCommand("warp").setTabCompleter(warpCommand);
        getCommand("spawn").setExecutor(spawnCommand);
        getCommand("vanish").setExecutor(vanishCommand);
        getCommand("voucher").setExecutor(voucherCommand);

        getServer().getPluginManager().registerEvents(chatEvent, this);
        getServer().getPluginManager().registerEvents(welcomeEvent, this);
        getServer().getPluginManager().registerEvents(tabJoinEvent, this);
        getServer().getPluginManager().registerEvents(vanishEvent, this);
        getServer().getPluginManager().registerEvents(telepathyEvent, this);
        getServer().getPluginManager().registerEvents(spawnPointEvent, this);
        getServer().getPluginManager().registerEvents(voucherUseEvent, this);

        // TAB Update
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                tab.updateTab(player);
            }
        }, 0L, 20L * 1); // every 1 second

        // Print to console
        getLogger().info("[MiningServerPlugin] Plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("[MiningServerPlugin] Plugin has been disabled!");
    }
}
