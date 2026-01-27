package icewizard7.miningServerPlugin.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.TabExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WarpCommand implements TabExecutor {
    private final Plugin plugin;

    public WarpCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(Component.text(
                    "Only players can execute this command.", NamedTextColor.RED
            ));
            return true;
        }

        Player player = (Player) commandSender;

        if (strings.length == 0) {
            player.sendMessage(Component.text(
                    "Usage: /warp <location>", NamedTextColor.RED
            ));
            return true;
        }

        String warpName = strings[0].toLowerCase();

        if (!(player.hasPermission("miningServerPlugin.warp." + warpName))) {
            player.sendMessage(Component.text(
                    "You do not have permission to warp there.", NamedTextColor.RED
            ));
            return true;
        }

        FileConfiguration config = plugin.getConfig();

        // Load warp from config
        if (!config.isConfigurationSection("warps." + warpName)) {
            player.sendMessage(Component.text("That warp does not exist.", NamedTextColor.RED));
            return true;
        }

        String worldName = config.getString("warps." + warpName + ".world");
        double x = config.getDouble("warps." + warpName + ".x");
        double y = config.getDouble("warps." + warpName + ".y");
        double z = config.getDouble("warps." + warpName + ".z");
        float yaw = (float) config.getDouble("warps." + warpName + ".yaw");
        float pitch = (float) config.getDouble("warps." + warpName + ".pitch");

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            player.sendMessage(Component.text("The world for this warp does not exist.", NamedTextColor.RED));
            return true;
        }

        Location warpLocation = new Location(world, x + 0.5, y, z + 0.5, yaw, pitch);
        player.teleport(warpLocation);
        player.sendMessage(Component.text("Warped to " + warpName + ".", NamedTextColor.GREEN));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] strings) {
        if (strings.length == 1) {
            FileConfiguration config = plugin.getConfig();

            if (!config.isConfigurationSection("warps")) {
                return new ArrayList<>();
            }

            List<String> warps = new ArrayList<>(config.getConfigurationSection("warps").getKeys(false));

            // Only suggest warps the player has permission for
            if (sender instanceof Player player) {
                warps = warps.stream()
                        .filter(warp -> player.hasPermission("miningServerPlugin.warp." + warp))
                        .collect(Collectors.toList());
            }

            String current = strings[0].toLowerCase();

            return warps.stream()
                    .filter(warp -> warp.toLowerCase().startsWith(current))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}
