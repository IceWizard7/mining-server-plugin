package icewizard7.miningServerPlugin.events;

import icewizard7.miningServerPlugin.managers.*;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;

import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class JoinQuitListener implements Listener {
    private final TabManager tabManager;
    private final StatManager statManager;
    private final JoinQuitManager joinQuitManager;
    private final VanishManager vanishManager;
    private final CombatManager combatManager;
    private final NameTagManager nameTagManager;

    public JoinQuitListener(
            TabManager tabManager, StatManager statManager,
            JoinQuitManager joinQuitManager, VanishManager vanishManager,
            CombatManager combatManager, NameTagManager nameTagManager
    ) {
        this.tabManager = tabManager;
        this.statManager = statManager;
        this.joinQuitManager = joinQuitManager;
        this.vanishManager = vanishManager;
        this.combatManager = combatManager;
        this.nameTagManager = nameTagManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        tabManager.joinEvent();
        statManager.joinEvent();
        joinQuitManager.joinEvent(player);
        nameTagManager.updateNameTag(player);
        vanishManager.joinEvent(player);

        event.joinMessage(null);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        joinQuitManager.quitEvent(player, vanishManager.isPlayerVanished(player));
        combatManager.quitEvent(player);
        nameTagManager.quitEvent(player);
        vanishManager.quitEvent(player);

        event.quitMessage(null);
    }
}
