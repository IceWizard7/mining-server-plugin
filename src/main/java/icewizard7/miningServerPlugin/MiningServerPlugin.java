package icewizard7.miningServerPlugin;

import icewizard7.miningServerPlugin.commands.*;
import icewizard7.miningServerPlugin.events.*;
import icewizard7.miningServerPlugin.managers.*;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import net.luckperms.api.LuckPerms;
import com.sk89q.worldguard.WorldGuard;

public final class MiningServerPlugin extends JavaPlugin {
    // Plugins
    private LuckPerms luckPerms;
    private WorldGuard worldGuard;

    // Mangers
    private WorldGuardManager worldGuardManager;
    private DiscordBridgeManager discordBridgeManager;
    private VanishManager vanishManager;
    private CombatManager combatManager;
    private LuckPermsManager luckPermsManager;
    private StatManager statManager;
    private VoidDamageManager voidDamageManager;
    private JoinQuitMessageManager joinQuitMessageManager;
    private TabManager tabManager;
    private ChatManager chatManager;
    private NameTagManager nameTagManager;
    private LeaderboardManager leaderboardManager;
    private PortalManager portalManager;
    private SpawnManager spawnManager;
    private AutoCompressManager autoCompressManager;
    private TelepathyManager telepathyManager;
    private VoucherManager voucherManager;
    private ShulkerViewManager shulkerViewManager;

    @Override
    public void onEnable() {
        // Save config.yml to plugins folder
        saveDefaultConfig();
        if (!loadDependencies()) return;

        initManagers();
        registerCommands();
        registerListeners();
        startSystems();

        // Print to console
        getLogger().info("MiningServerPlugin has been enabled.");
    }

    private boolean loadDependencies() {
        this.luckPerms = getServer().getServicesManager().load(LuckPerms.class);
        if (luckPerms == null) {
            getLogger().severe("LuckPerms not found. Disabling MiningServerPlugin.");
            getServer().getPluginManager().disablePlugin(this);
            return false;
        }

        this.worldGuard = WorldGuard.getInstance();

        if (worldGuard == null) {
            getLogger().severe("WorldGuard not found. Disabling MiningServerPlugin.");
            getServer().getPluginManager().disablePlugin(this);
            return false;
        }

        if (getServer().getPluginManager().getPlugin("DecentHolograms") == null) {
            getLogger().severe("DecentHolograms not found. Disabling MiningServerPlugin.");
            getServer().getPluginManager().disablePlugin(this);
            return false;
        }

        return true;
    }

    private void registerCommand(String name, CommandExecutor executor) {
        PluginCommand cmd = getCommand(name);
        if (cmd == null) {
            getLogger().severe("Command '" + name + "' missing from plugin.yml.");
            return;
        }
        cmd.setExecutor(executor);
    }

    private void registerTabExecutorCommand(String name, TabExecutor executor) {
        PluginCommand cmd = getCommand(name);
        if (cmd == null) {
            getLogger().severe("Command '" + name + "' missing from plugin.yml.");
            return;
        }
        cmd.setExecutor(executor);
        cmd.setTabCompleter(executor);
    }

    private void registerListener(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }

    private void initManagers() {
        // Core world managers
        this.worldGuardManager = new WorldGuardManager(worldGuard);
        this.luckPermsManager = new LuckPermsManager(luckPerms);
        this.portalManager = new PortalManager(this);
        this.spawnManager = new SpawnManager(this);

        // External systems
        this.discordBridgeManager = new DiscordBridgeManager(this);

        // Core player managers
        this.statManager = new StatManager(this);
        this.joinQuitMessageManager = new JoinQuitMessageManager(discordBridgeManager, statManager);
        this.vanishManager = new VanishManager(this, joinQuitMessageManager);
        this.combatManager = new CombatManager(this, worldGuardManager);
        this.voidDamageManager = new VoidDamageManager();

        // Visual systems
        this.chatManager = new ChatManager(discordBridgeManager, luckPerms);
        this.tabManager = new TabManager(this, vanishManager, luckPermsManager);
        this.nameTagManager = new NameTagManager(this, luckPerms, luckPermsManager, statManager);
        this.leaderboardManager = new LeaderboardManager(this, statManager, luckPermsManager);

        // Economy & Items
        this.autoCompressManager = new AutoCompressManager(this);
        this.telepathyManager = new TelepathyManager(this, autoCompressManager);
        this.voucherManager = new VoucherManager(luckPerms, this);
        this.shulkerViewManager = new ShulkerViewManager();
    }

    private void registerCommands() {
        registerCommand("info", new InfoCommand());
        registerCommand("rules", new RulesCommand());
        registerCommand("discord", new DiscordCommand());
        registerCommand("god", new GodCommand());
        registerCommand("invsee", new InvseeCommand());
        registerCommand("fly", new FlyCommand(combatManager));
        registerCommand("flyspeed", new FlySpeedCommand(combatManager));
        registerCommand("autocompress", new AutoCompressCommand(autoCompressManager));
        registerCommand("spawn", new SpawnCommand(this, combatManager));
        registerCommand("vanish", new VanishCommand(this, vanishManager));
        registerCommand("voucher", new VoucherCommand(voucherManager));
        registerCommand("link", new LinkCommand(discordBridgeManager));
        registerCommand("unlink", new UnlinkCommand(discordBridgeManager));
        registerCommand("enderchest", new EnderChestCommand());
        registerTabExecutorCommand("warp", new WarpCommand(this, combatManager));
        registerTabExecutorCommand("show", new ShowCommand(this));
    }

    private void registerListeners() {
        registerListener(new ChatListener(chatManager));
        registerListener(new DamageListener(combatManager, voidDamageManager));
        registerListener(new PlayerInteractListener(combatManager, voucherManager, shulkerViewManager));
        registerListener(new PlayerLifecycleListener(tabManager, statManager, joinQuitMessageManager, vanishManager, combatManager, nameTagManager, spawnManager));
        registerListener(new WorldListener(statManager, portalManager, spawnManager, voidDamageManager, telepathyManager));
    }

    private void startSystems() {
        tabManager.startTabTask();
        combatManager.startCombatTask();
        statManager.startScoreboardTask();
        statManager.startAutoSave();
        nameTagManager.startNameTagTask();
        leaderboardManager.startAllLeaderboards();
        discordBridgeManager.connect();
    }

    @Override
    public void onDisable() {
        getLogger().info("MiningServerPlugin is disabling...");

        // Check if not null in case enable failed
        if (combatManager != null) combatManager.shutdown();
        if (tabManager != null) tabManager.shutdown();
        if (statManager != null) statManager.shutdown();
        if (nameTagManager != null) nameTagManager.shutdown();
        if (leaderboardManager != null) leaderboardManager.shutdown();
        if (discordBridgeManager != null) discordBridgeManager.shutdown();

        getLogger().info("MiningServerPlugin has been disabled.");
    }
}
