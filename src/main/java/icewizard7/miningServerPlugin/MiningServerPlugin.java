package icewizard7.miningServerPlugin;

import icewizard7.miningServerPlugin.commands.*;
import icewizard7.miningServerPlugin.events.*;
import icewizard7.miningServerPlugin.utils.*;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import net.luckperms.api.LuckPerms;

public final class MiningServerPlugin extends JavaPlugin {
    private LuckPerms luckPerms;
    private VanishManager vanishManager;
    private TAB tab;
    private NameTagManager nameTagManager;
    private PortalManager portalManager;
    private DiscordLinkManager discordLinkManager;
    private DiscordBridge discordBridge;
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

    private void registerListener(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }

    private void initManagers() {
        this.vanishManager = new VanishManager(this);
        this.tab = new TAB(this, vanishManager, luckPerms);
        this.nameTagManager = new NameTagManager(this, luckPerms);
        this.portalManager = new PortalManager(this);
        this.discordLinkManager = new DiscordLinkManager(this);
        this.discordBridge = new DiscordBridge(this, discordLinkManager);
        discordBridge.connect();
        this.combatManager = new CombatManager(this);
        this.autoCompressManager = new AutoCompressManager(this);
        this.voucherManager = new VoucherManager(this);
    }

    private void registerCommands() {
        InfoCommand infoCommand = new InfoCommand();
        RulesCommand rulesCommand = new RulesCommand();
        DiscordCommand discordCommand = new DiscordCommand();
        GodCommand godCommand = new GodCommand();
        InvseeCommand invseeCommand = new InvseeCommand();
        FlyCommand flyCommand = new FlyCommand(combatManager);
        AutoCompressCommand autoCompressCommand = new AutoCompressCommand(autoCompressManager);
        WarpCommand warpCommand = new WarpCommand(this, combatManager);
        SpawnCommand spawnCommand = new SpawnCommand(this, combatManager);
        VanishCommand vanishCommand = new VanishCommand(this, vanishManager);
        VoucherCommand voucherCommand = new VoucherCommand(luckPerms, voucherManager);
        LinkCommand linkCommand = new LinkCommand(discordLinkManager, discordBridge);
        UnlinkCommand unlinkCommand = new UnlinkCommand(discordLinkManager);

        registerCommand("info", infoCommand);
        registerCommand("rules", rulesCommand);
        registerCommand("discord", discordCommand);
        registerCommand("god", godCommand);
        registerCommand("invsee", invseeCommand);
        registerCommand("fly", flyCommand);
        registerCommand("autocompress", autoCompressCommand);
        registerCommand("warp", warpCommand);
        registerCommand("warp", warpCommand);
        registerCommand("spawn", spawnCommand);
        registerCommand("vanish", vanishCommand);
        registerCommand("voucher", voucherCommand);
        registerCommand("link", linkCommand);
        registerCommand("unlink", unlinkCommand);
    }

    private void registerListeners() {
        Listener chatListener = new ChatListener(discordBridge, luckPerms);
        Listener welcomeListener = new WelcomeListener(discordBridge);
        Listener tabJoinListener = new TabJoinListener(tab);
        Listener vanishListener = new VanishListener(this, vanishManager);
        Listener telepathyListener = new TelepathyListener(this, autoCompressManager);
        Listener spawnListener = new SpawnListener(this);
        Listener voucherUseListener = new VoucherUseListener(luckPerms, voucherManager);
        Listener nameTagListener = new NameTagListener(nameTagManager);
        Listener portalListener = new PortalListener(portalManager);
        Listener combatListener = new CombatListener(combatManager);

        registerListener(chatListener);
        registerListener(welcomeListener);
        registerListener(tabJoinListener);
        registerListener(vanishListener);
        registerListener(telepathyListener);
        registerListener(spawnListener);
        registerListener(voucherUseListener);
        registerListener(nameTagListener);
        registerListener(portalListener);
        registerListener(combatListener);
    }

    private void startTasks() {
        nameTagManager.startNameTagTask();
        tab.startTabTask();
        combatManager.startCombatTask();
    }

    @Override
    public void onDisable() {
        getLogger().info("MiningServerPlugin is disabling...");

        // Check if not null in case enable failed
        if (combatManager != null) combatManager.shutdown();
        if (nameTagManager != null) nameTagManager.shutdown();
        if (tab != null) tab.shutdown();
        if (discordBridge != null) discordBridge.shutdown();

        getLogger().info("MiningServerPlugin has been disabled.");
    }
}
