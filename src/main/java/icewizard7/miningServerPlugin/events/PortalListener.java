package icewizard7.miningServerPlugin.events;

import icewizard7.miningServerPlugin.classes.Portal;
import icewizard7.miningServerPlugin.utils.PortalManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PortalListener implements Listener {

    private final PortalManager portalManager;
    private final Map<UUID, Long> cooldown = new HashMap<>();

    public PortalListener(PortalManager portalManager) {
        this.portalManager = portalManager;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();

        // Prevent spam teleport loop
        if (cooldown.containsKey(player.getUniqueId())) {
            if (System.currentTimeMillis() - cooldown.get(player.getUniqueId()) < 2000) {
                return;
            }
        }

        Location loc = player.getLocation();

        for (Portal portal : portalManager.getPortals()) {

            if (!loc.getWorld().equals(portal.fromCenter.getWorld())) continue;

            if (loc.distanceSquared(portal.fromCenter) <= portal.radius * portal.radius) {

                // Teleport
                player.teleport(portal.to);

                // Effects
                player.spawnParticle(portal.particle, portal.to, portal.particleCount);
                player.playSound(portal.to, portal.sound, 1f, 1f);

                cooldown.put(player.getUniqueId(), System.currentTimeMillis());
                break;
            }
        }
    }
}
