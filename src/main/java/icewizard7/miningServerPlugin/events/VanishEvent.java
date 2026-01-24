package icewizard7.miningServerPlugin.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.Set;

public class VanishEvent implements Listener {
    private final Plugin plugin;
    private final Set<Player> vanishedPlayers;

    public VanishEvent(Plugin plugin, Set<Player> vanishedPlayers) {
        this.plugin = plugin;
        this.vanishedPlayers = vanishedPlayers;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        for (Player vanishedPlayer : vanishedPlayers) {
            player.hidePlayer(plugin, vanishedPlayer);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        vanishedPlayers.remove(player);
    }
}
