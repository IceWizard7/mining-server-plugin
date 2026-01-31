package icewizard7.miningServerPlugin.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.entity.Player;

public class DiscordCommand implements CommandExecutor{
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
            return true;
        }

        if (player.hasPermission("miningServerPlugin.discord")) {
            Component result = Component.text("Discord Invite Link: ", NamedTextColor.GOLD)
                    .append(Component.text("discord.gg/8ce6N8QXpm", NamedTextColor.WHITE))
                    .clickEvent(ClickEvent.openUrl("discord.gg/8ce6N8QXpm"))
                    .hoverEvent(HoverEvent.showText(Component.text("Click to open Discord invitation!", NamedTextColor.YELLOW)));

            player.sendMessage(result);
            return true;
        }

        player.sendMessage(Component.text(
                "You do not have permission to use this command.", NamedTextColor.RED
        ));
        return true;
    }
}
