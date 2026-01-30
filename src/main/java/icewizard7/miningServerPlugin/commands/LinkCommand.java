package icewizard7.miningServerPlugin.commands;

import icewizard7.miningServerPlugin.utils.DiscordBridge;
import icewizard7.miningServerPlugin.utils.DiscordLinkManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LinkCommand implements CommandExecutor {
    private final DiscordLinkManager discordLinkManager;
    private final DiscordBridge discordBridge;

    public LinkCommand(DiscordLinkManager discordLinkManager, DiscordBridge discordBridge) {
        this.discordLinkManager = discordLinkManager;
        this.discordBridge = discordBridge;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String string, String[] args) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
            return true;
        }

        if (!(player.hasPermission("miningServerPlugin.link"))) {
            player.sendMessage(Component.text(
                    "You do not have permission to use this command.", NamedTextColor.RED
            ));
            return true;
        }

        String code = discordLinkManager.createCode(player.getUniqueId());
        String botName = discordBridge.getBotName();

        player.sendMessage(Component.text("Discord Account Linking", NamedTextColor.GOLD));
        player.sendMessage(Component.text("Open Discord and DM the bot ", NamedTextColor.GRAY)
                .append(Component.text(botName, NamedTextColor.AQUA)));

        player.sendMessage(Component.text("Send this command:", NamedTextColor.GRAY));

        player.sendMessage(
                Component.text("!link " + code, NamedTextColor.YELLOW)
                        .clickEvent(ClickEvent.copyToClipboard("!link " + code))
                        .hoverEvent(HoverEvent.showText(Component.text("Click to copy")))
        );

        player.sendMessage(Component.text("This code expires after use.", NamedTextColor.DARK_GRAY));

        return true;
    }
}
