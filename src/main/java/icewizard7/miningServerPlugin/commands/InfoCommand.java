package icewizard7.miningServerPlugin.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.ConsoleCommandSender;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class InfoCommand implements CommandExecutor{
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof ConsoleCommandSender) {
            commandSender.sendMessage(Component.text(
                    "[Console] Plugin by IceWizard7", NamedTextColor.WHITE
            ));
            return true;
        }

        if (commandSender.hasPermission("miningServerPlugin.info")) {
            commandSender.sendMessage(Component.text(
                    "Plugin by IceWizard7", NamedTextColor.WHITE
            ));
            return true;
        }

        commandSender.sendMessage(Component.text(
                "You do not have permission to use this command.", NamedTextColor.RED
        ));
        return true;
    }
}
