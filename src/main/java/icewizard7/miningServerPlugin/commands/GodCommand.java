package icewizard7.miningServerPlugin.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GodCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(Component.text(
                    "Only players can execute this command.", NamedTextColor.RED
            ));
        }

        Player player = (Player) commandSender;

        if (!(player.hasPermission("miningServerPlugin.god"))) {
            player.sendMessage(Component.text(
                    "You do not have permission to use this command.", NamedTextColor.RED
            ));
        }

        if (player.getGameMode() == GameMode.CREATIVE) {
            player.sendMessage(Component.text(
                    "Already in creative mode.", NamedTextColor.RED
            ));
            return true;
        }

        if (player.getGameMode() == GameMode.SPECTATOR) {
            player.sendMessage(Component.text(
                    "Already in spectator mode.", NamedTextColor.RED
            ));
            return true;
        }

        if (player.isInvulnerable()){
            player.setInvulnerable(false);
            player.sendMessage(Component.text(
                    "God mode disabled.", NamedTextColor.RED
            ));
        } else {
            player.setInvulnerable(true);
            player.sendMessage(Component.text(
                    "God mode enabled.", NamedTextColor.GREEN
            ));
        }

        return true;
    }
}
