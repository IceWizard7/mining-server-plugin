package icewizard7.miningServerPlugin.managers;

import icewizard7.miningServerPlugin.classes.Portal;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class PortalManager {

    private final List<Portal> portals = new ArrayList<>();
    private final Plugin plugin;

    public PortalManager(Plugin plugin) {
        this.plugin = plugin;
        loadPortals();
    }

    public void loadPortals() {
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

    public List<Portal> getPortals() {
        return portals;
    }
}
