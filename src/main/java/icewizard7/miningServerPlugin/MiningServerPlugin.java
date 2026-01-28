package icewizard7.miningServerPlugin;

import icewizard7.miningServerPlugin.commands.*;
import icewizard7.miningServerPlugin.events.*;
import icewizard7.miningServerPlugin.utils.*;

import net.luckperms.api.model.user.User;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;

import net.luckperms.api.LuckPerms;

import java.util.UUID;
import java.util.HashSet;
import java.util.Set;

public final class MiningServerPlugin extends JavaPlugin {
    // Set of vanished Players
    private final Set<UUID> vanishedPlayers = new HashSet<>();

    // TAB
    private TAB tab;
    private NameTagManager nameTagManager;

    @Override
    public void onEnable() {
        // Save config.yml to plugins folder
        saveDefaultConfig();

        // Load LuckPerms API
        LuckPerms luckPerms = getServer().getServicesManager().load(LuckPerms.class);
        if (luckPerms == null) {
            getLogger().severe("[MiningServerPlugin] LuckPerms not found! Disabling MiningServerPlugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize TAB
        this.tab = new TAB(vanishedPlayers, luckPerms);
        this.nameTagManager = new NameTagManager(luckPerms);

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
        Listener nameTagJoinEvent = new NameTagEvent(nameTagManager);

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
        getServer().getPluginManager().registerEvents(nameTagJoinEvent, this);

        // Rank changes
        luckPerms.getEventBus().subscribe(this,
                net.luckperms.api.event.user.UserDataRecalculateEvent.class,
                event -> Bukkit.getScheduler().runTask(this, () -> {
                    Player player = Bukkit.getPlayer(event.getUser().getUniqueId());
                    if (player != null) {
                        nameTagManager.updateNameTag(player);
                    }
                })
        );

        luckPerms.getEventBus().subscribe(this,
                net.luckperms.api.event.group.GroupDataRecalculateEvent.class,
                event -> Bukkit.getScheduler().runTask(this, () -> {

                    String groupName = event.getGroup().getName();

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
                        if (user == null) continue;

                        if (user.getInheritedGroups(user.getQueryOptions()).stream()
                                .anyMatch(g -> g.getName().equalsIgnoreCase(groupName))) {

                            nameTagManager.updateNameTag(player);
                        }
                    }
                })
        );

        // TAB Update
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                tab.updateTab(player);
            }
        }, 0L, 20L * 3); // every 3 seconds

        // Print to console
        getLogger().info("[MiningServerPlugin] Plugin has been enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("[MiningServerPlugin] Plugin has been disabled.");
    }
}
