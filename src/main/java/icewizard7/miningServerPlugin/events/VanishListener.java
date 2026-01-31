package icewizard7.miningServerPlugin.events;

import icewizard7.miningServerPlugin.utils.VanishManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.Set;
import java.util.UUID;

public class VanishListener implements Listener {
    private final Plugin plugin;
    private final VanishManager vanishManager;

    public VanishListener(Plugin plugin, VanishManager vanishManager) {
        this.plugin = plugin;
        this.vanishManager = vanishManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        for (UUID vanishedPlayerUUID : vanishManager.getVanishedPlayers()) {
            Player vanishedPlayer = Bukkit.getPlayer(vanishedPlayerUUID);
            if (vanishedPlayer != null) {
                player.hidePlayer(plugin, vanishedPlayer);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        vanishManager.removeVanishedPlayer(player);
    }
}
