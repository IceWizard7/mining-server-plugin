package icewizard7.miningServerPlugin.commands;

import icewizard7.miningServerPlugin.utils.CombatManager;
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
    private final CombatManager combatManager;

    public WarpCommand(Plugin plugin, CombatManager combatManager) {
        this.plugin = plugin;
        this.combatManager = combatManager;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Component.text(
                    "Only players can execute this command.", NamedTextColor.RED
            ));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(Component.text(
                    "Usage: /warp <location>", NamedTextColor.RED
            ));
            return true;
        }

        String warpName = args[0].toLowerCase();

        if (!(player.hasPermission("miningServerPlugin.warp." + warpName))) {
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
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        if (args.length != 1) {
            return new ArrayList<>();
        }

        FileConfiguration config = plugin.getConfig();

        if (!config.isConfigurationSection("warps")) {
            return new ArrayList<>();
        }

        List<String> warps = new ArrayList<>(config.getConfigurationSection("warps").getKeys(false));

        // Only suggest warps the player has permission for
        if (commandSender instanceof Player player) {
            warps = warps.stream()
                    .filter(warp -> player.hasPermission("miningServerPlugin.warp." + warp))
                    .collect(Collectors.toList());
        }

        String current = args[0].toLowerCase();

        return warps.stream()
                .filter(warp -> warp.toLowerCase().startsWith(current))
                .collect(Collectors.toList());
    }
}
