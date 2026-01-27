package icewizard7.miningServerPlugin.events;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.Set;
import java.util.UUID;

public class VanishEvent implements Listener {
    private final Plugin plugin;
    private final Set<UUID> vanishedPlayers;

    public VanishEvent(Plugin plugin, Set<UUID> vanishedPlayers) {
        this.plugin = plugin;
        this.vanishedPlayers = vanishedPlayers;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        for (UUID vanishedPlayerUUID : vanishedPlayers) {
            Player vanishedPlayer = Bukkit.getPlayer(vanishedPlayerUUID);
            if (vanishedPlayer != null) {
                player.hidePlayer(plugin, vanishedPlayer);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        vanishedPlayers.remove(player.getUniqueId());
    }
}
