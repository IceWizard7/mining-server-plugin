package icewizard7.miningServerPlugin.events;

import icewizard7.miningServerPlugin.managers.JoinQuitManager;
import icewizard7.miningServerPlugin.managers.VanishManager;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class WelcomeListener implements Listener {

    private final JoinQuitManager joinQuitManager;
    private final VanishManager vanishManager;

    public WelcomeListener(JoinQuitManager joinQuitManager, VanishManager vanishManager) {
        this.joinQuitManager = joinQuitManager;
        this.vanishManager = vanishManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        joinQuitManager.joinEvent(player);
        event.joinMessage(null);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        joinQuitManager.quitEvent(player, vanishManager.isPlayerVanished(player));

        event.quitMessage(null);
    }
}
