package icewizard7.miningServerPlugin.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

public class DiscordCommand implements CommandExecutor{
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender.hasPermission("miningServerPlugin.discord")) {
            Component result = Component.text("Discord Invite Link: ", NamedTextColor.GOLD)
                    .append(Component.text("discord.gg/8ce6N8QXpm", NamedTextColor.WHITE))
                    .clickEvent(ClickEvent.openUrl("discord.gg/8ce6N8QXpm"))
                    .hoverEvent(HoverEvent.showText(Component.text("Click to open Discord invitation!", NamedTextColor.YELLOW)));

            commandSender.sendMessage(result);
            return true;
        }

        commandSender.sendMessage(Component.text(
                "You do not have permission to use this command.", NamedTextColor.RED
        ));
        return true;
    }
}
