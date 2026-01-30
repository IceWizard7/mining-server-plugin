package icewizard7.miningServerPlugin.commands;

import icewizard7.miningServerPlugin.utils.DiscordLinkManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnlinkCommand implements CommandExecutor {

    private final DiscordLinkManager discordLinkManager;

    public UnlinkCommand(DiscordLinkManager discordLinkManager) {
        this.discordLinkManager = discordLinkManager;
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

        if (!discordLinkManager.isLinked(player.getUniqueId())) {
            player.sendMessage(Component.text("Your account is not linked to any Discord account.", NamedTextColor.RED));
            return true;
        }

        discordLinkManager.unlink(player.getUniqueId());
        player.sendMessage(Component.text("Your Discord account link has been removed.", NamedTextColor.GREEN));
        return true;
    }
}
