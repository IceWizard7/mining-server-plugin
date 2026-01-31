package icewizard7.miningServerPlugin.events;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;

import icewizard7.miningServerPlugin.utils.TabManager;

public class TabJoinListener implements Listener {
    private final TabManager tabManager;

    public TabJoinListener(TabManager tabManager) {
        this.tabManager = tabManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Newly joined player + all players -> player count
            tabManager.updateTab(player);
        }
    }
}
