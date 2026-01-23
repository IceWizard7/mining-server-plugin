package icewizard7.miningServerPlugin.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AutoCompressCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(Component.text(
                    "Only players can execute this command.", NamedTextColor.RED
            ));
            return true;
        }

        Player player = (Player) commandSender;

        if (!(player.hasPermission("miningServerPlugin.autocompress"))) {
            player.sendMessage(Component.text(
                    "You do not have permission to use this command.", NamedTextColor.RED
            ));
            return true;
        }

        return true;
    }
}
