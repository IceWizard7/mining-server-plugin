package icewizard7.miningServerPlugin.events;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;

import icewizard7.miningServerPlugin.utils.TAB;

public class TabJoinEvent implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Newly joined player + all players -> player count
            TAB.updateTab(player);
        }
    }
}
