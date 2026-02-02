package icewizard7.miningServerPlugin.events;

import icewizard7.miningServerPlugin.managers.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerLifecycleListener implements Listener {
    private final TabManager tabManager;
    private final StatManager statManager;
    private final JoinQuitMessageManager joinQuitMessageManager;
    private final VanishManager vanishManager;
    private final CombatManager combatManager;
    private final NameTagManager nameTagManager;
    private final SpawnManager spawnManager;

    public PlayerLifecycleListener(
            TabManager tabManager, StatManager statManager,
            JoinQuitMessageManager joinQuitMessageManager, VanishManager vanishManager,
            CombatManager combatManager, NameTagManager nameTagManager,
            SpawnManager spawnManager
    ) {
        this.tabManager = tabManager;
        this.statManager = statManager;
        this.joinQuitMessageManager = joinQuitMessageManager;
        this.vanishManager = vanishManager;
        this.combatManager = combatManager;
        this.nameTagManager = nameTagManager;
        this.spawnManager = spawnManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        tabManager.joinEvent(event);
        statManager.joinEvent(event);
        nameTagManager.joinEvent(event);
        vanishManager.joinEvent(event);
        spawnManager.joinEvent(event);
        joinQuitMessageManager.joinEvent(event);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        combatManager.deathEvent(event);
        statManager.deathEvent(event);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        spawnManager.respawnEvent(event);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        combatManager.quitEvent(event);
        nameTagManager.quitEvent(event);
        vanishManager.quitEvent(event);
        statManager.quitEvent(event);
        joinQuitMessageManager.quitEvent(event);
    }
}
