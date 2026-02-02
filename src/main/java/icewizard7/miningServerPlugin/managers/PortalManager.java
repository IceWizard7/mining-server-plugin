package icewizard7.miningServerPlugin.managers;

import icewizard7.miningServerPlugin.classes.Portal;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class PortalManager {

    private final List<Portal> portals = new ArrayList<>();
    private final Map<UUID, Long> cooldown = new HashMap<>();
    private final Plugin plugin;

    public PortalManager(Plugin plugin) {
        this.plugin = plugin;
        loadPortals();
    }

    private void loadPortals() {
        portals.clear();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("portals");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            ConfigurationSection pSec = section.getConfigurationSection(key);

            ConfigurationSection from = pSec.getConfigurationSection("from");
            ConfigurationSection to = pSec.getConfigurationSection("to");

            World fromWorld = Bukkit.getWorld(from.getString("world"));
            World toWorld = Bukkit.getWorld(to.getString("world"));

            Location fromLoc = new Location(fromWorld,
                    from.getDouble("x"),
                    from.getDouble("y"),
                    from.getDouble("z"));

            Location toLoc = new Location(toWorld,
                    to.getDouble("x"),
                    to.getDouble("y"),
                    to.getDouble("z"),
                    (float) to.getDouble("yaw"),
                    (float) to.getDouble("pitch"));


            Sound sound = Registry.SOUNDS.get(NamespacedKey.minecraft(pSec.getString("sound").toLowerCase()));
            Particle particle = Registry.PARTICLE_TYPE.get(NamespacedKey.minecraft(pSec.getString("particle").toLowerCase()));

            portals.add(new Portal(
                    fromLoc,
                    from.getDouble("radius"),
                    toLoc,
                    particle,
                    pSec.getInt("particle_count"),
                    sound
            ));
        }
    }

    public void moveEvent(PlayerMoveEvent e) {
        Player player = e.getPlayer();

        // Prevent spam teleport loop
        if (cooldown.containsKey(player.getUniqueId())) {
            if (System.currentTimeMillis() - cooldown.get(player.getUniqueId()) < 2000) {
                return;
            }
        }

        Location loc = player.getLocation();

        for (Portal portal : portals) {

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
