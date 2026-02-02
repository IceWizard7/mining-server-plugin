package icewizard7.miningServerPlugin.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;

public class SpawnManager {
    private final Location globalSpawn;
    private final Plugin plugin;

    public SpawnManager(Plugin plugin) {
        this.plugin = plugin;
        this.globalSpawn = loadSpawn();
    }

    private Location loadSpawn() {
        FileConfiguration config = plugin.getConfig();

        String worldName = config.getString("spawn.world");
        double x = config.getDouble("spawn.x");
        double y = config.getDouble("spawn.y");
        double z = config.getDouble("spawn.z");
        float yaw = (float) config.getDouble("spawn.yaw");
        float pitch = (float) config.getDouble("spawn.pitch");

        World world = Bukkit.getWorld(worldName);

        return new Location(world, x + 0.5, y, z + 0.5, yaw, pitch);
    }

    public void joinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Delay 1 tick so Minecraft doesn't fight the teleport
        Bukkit.getScheduler().runTask(this.plugin, () -> {
            player.teleport(globalSpawn);
        });
    }

    public void respawnEvent(PlayerRespawnEvent event) {
        // This OVERRIDES bed / anchor spawn
        event.setRespawnLocation(globalSpawn);
    }

    public void bedEnterEvent(PlayerBedEnterEvent event) {
        Player player = event.getPlayer();
        player.sendMessage(Component.text("Beds are useless. Accept your fate.", NamedTextColor.RED));
    }
}
