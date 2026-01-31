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

public final class MiningServerPlugin extends JavaPlugin {
    private LuckPerms luckPerms;
    private VanishManager vanishManager;
    private TabManager tabManager;
    private NameTagManager nameTagManager;
    private PortalManager portalManager;
    private DiscordLinkManager discordLinkManager;
    private DiscordBridgeManager discordBridgeManager;
    private CombatManager combatManager;
    private AutoCompressManager autoCompressManager;
    private VoucherManager voucherManager;

    @Override
    public void onEnable() {
        // Save config.yml to plugins folder
        saveDefaultConfig();

        // Load LuckPerms API
        this.luckPerms = getServer().getServicesManager().load(LuckPerms.class);
        if (luckPerms == null) {
            getLogger().severe("LuckPerms not found. Disabling MiningServerPlugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        initManagers();
        registerCommands();
        registerListeners();
        startTasks();

        // Print to console
        getLogger().info("MiningServerPlugin has been enabled.");
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
        this.vanishManager = new VanishManager(this);
        this.tabManager = new TabManager(this, vanishManager, luckPerms);
        this.nameTagManager = new NameTagManager(this, luckPerms);
        this.portalManager = new PortalManager(this);
        this.discordLinkManager = new DiscordLinkManager(this);
        this.discordBridgeManager = new DiscordBridgeManager(this, discordLinkManager);
        discordBridgeManager.connect();
        this.combatManager = new CombatManager(this);
        this.autoCompressManager = new AutoCompressManager(this);
        this.voucherManager = new VoucherManager(this);
    }

    private void registerCommands() {
        registerCommand("info", new InfoCommand());
        registerCommand("rules", new RulesCommand());
        registerCommand("discord", new DiscordCommand());
        registerCommand("god", new GodCommand());
        registerCommand("invsee", new InvseeCommand());
        registerCommand("fly", new FlyCommand(combatManager));
        registerCommand("autocompress", new AutoCompressCommand(autoCompressManager));
        registerCommand("spawn", new SpawnCommand(this, combatManager));
        registerCommand("vanish", new VanishCommand(this, vanishManager));
        registerCommand("voucher", new VoucherCommand(luckPerms, voucherManager));
        registerCommand("link", new LinkCommand(discordLinkManager, discordBridgeManager));
        registerCommand("unlink", new UnlinkCommand(discordLinkManager));
        registerTabExecutorCommand("warp", new WarpCommand(this, combatManager));
        registerTabExecutorCommand("show", new ShowCommand(this));
    }

    private void registerListeners() {
        registerListener(new ChatListener(discordBridgeManager, luckPerms));
        registerListener(new WelcomeListener(discordBridgeManager));
        registerListener(new TabJoinListener(tabManager));
        registerListener(new VanishListener(this, vanishManager));
        registerListener(new TelepathyListener(this, autoCompressManager));
        registerListener(new SpawnListener(this));
        registerListener(new VoucherUseListener(luckPerms, voucherManager));
        registerListener(new NameTagListener(nameTagManager));
        registerListener(new PortalListener(portalManager));
        registerListener(new CombatListener(combatManager));
    }

    private void startTasks() {
        nameTagManager.startNameTagTask();
        tabManager.startTabTask();
        combatManager.startCombatTask();
    }

    @Override
    public void onDisable() {
        getLogger().info("MiningServerPlugin is disabling...");

        // Check if not null in case enable failed
        if (combatManager != null) combatManager.shutdown();
        if (nameTagManager != null) nameTagManager.shutdown();
        if (tabManager != null) tabManager.shutdown();
        if (discordBridgeManager != null) discordBridgeManager.shutdown();

        getLogger().info("MiningServerPlugin has been disabled.");
    }
}
