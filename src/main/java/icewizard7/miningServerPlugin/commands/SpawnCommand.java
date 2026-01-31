package icewizard7.miningServerPlugin.commands;

import icewizard7.miningServerPlugin.utils.CombatManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.Bukkit;

public class SpawnCommand implements CommandExecutor {
    private final Plugin plugin;
    private final CombatManager combatManager;

    public SpawnCommand(Plugin plugin, CombatManager combatManager) {
        this.plugin = plugin;
        this.combatManager = combatManager;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
            return true;
        }

        if (!(player.hasPermission("miningServerPlugin.spawn"))) {
            player.sendMessage(Component.text(
                    "You do not have permission to warp there.", NamedTextColor.RED
            ));
            return true;
        }

        if (combatManager.isInCombat(player)) {
            combatManager.sendCombatMessage(player);
            return true;
        }

        FileConfiguration config = plugin.getConfig();

        // Load warp from config
        if (!config.isConfigurationSection("spawn")) {
            player.sendMessage(Component.text("That warp does not exist.", NamedTextColor.RED));
            return true;
        }

        String worldName = config.getString("spawn.world");
        double x = config.getDouble("spawn.x");
        double y = config.getDouble("spawn.y");
        double z = config.getDouble("spawn.z");
        float yaw = (float) config.getDouble("spawn.yaw");
        float pitch = (float) config.getDouble("spawn.pitch");

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            player.sendMessage(Component.text("The world for this warp does not exist.", NamedTextColor.RED));
            return true;
        }

        Location warpLocation = new Location(world, x + 0.5, y, z + 0.5, yaw, pitch);
        player.teleport(warpLocation);
        player.sendMessage(Component.text("Warped to spawn.", NamedTextColor.GREEN));

        return true;
    }
}
