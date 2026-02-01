package icewizard7.miningServerPlugin.events;

import icewizard7.miningServerPlugin.managers.StatManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;

import icewizard7.miningServerPlugin.managers.TabManager;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinQuitListener implements Listener {
    private final TabManager tabManager;
    private final StatManager statManager;

    public JoinQuitListener(TabManager tabManager, StatManager statManager) {
        this.tabManager = tabManager;
        this.statManager = statManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Newly joined player + all players -> player count
            tabManager.updateTab(player);
            statManager.updateBoard(player);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        statManager.quitEvent(event.getPlayer());
    }
}
