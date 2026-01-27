package icewizard7.miningServerPlugin.commands;

import org.bukkit.Bukkit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Set;
import java.util.UUID;

public class VanishCommand implements CommandExecutor {

    private final Plugin plugin;
    private final Set<UUID> vanishedPlayers;

    public VanishCommand(Plugin plugin, Set<UUID> vanishedPlayers) {
        this.plugin = plugin;
        this.vanishedPlayers = vanishedPlayers;
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

        if (!(player.hasPermission("miningServerPlugin.vanish"))) {
            player.sendMessage(Component.text(
                    "You do not have permission to use this command.", NamedTextColor.RED
            ));
            return true;
        }

        if (vanishedPlayers.contains(player.getUniqueId())) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.showPlayer(plugin, player);
            }
            fakeJoinMessage(player);
            vanishedPlayers.remove(player.getUniqueId());
            player.sendMessage(Component.text(
                    "Vanish mode disabled.", NamedTextColor.RED
            ));

        } else {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.hidePlayer(plugin, player);
            }
            fakeQuitMessage(player);
            vanishedPlayers.add(player.getUniqueId());
            player.sendMessage(Component.text(
                    "Vanish mode enabled.", NamedTextColor.GREEN
            ));
        }

        return true;
    }

    public void fakeJoinMessage(Player player) {
        Component message = Component.text("[", NamedTextColor.WHITE)
                .append(Component.text("+", NamedTextColor.GREEN))
                .append(Component.text("] " + player.getName(), NamedTextColor.WHITE));

        Bukkit.broadcast(message);
    }

    public void fakeQuitMessage(Player player) {
        Component message = Component.text("[", NamedTextColor.WHITE)
                .append(Component.text("-", NamedTextColor.RED))
                .append(Component.text("] " + player.getName(), NamedTextColor.WHITE));

        Bukkit.broadcast(message);
    }
}
