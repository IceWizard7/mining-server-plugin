package icewizard7.miningServerPlugin.commands;

import icewizard7.miningServerPlugin.utils.DiscordLinkManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LinkCommand implements CommandExecutor {
    private final DiscordLinkManager discordLinkManager;

    public LinkCommand(DiscordLinkManager discordLinkManager) {
        this.discordLinkManager = discordLinkManager;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String string, String[] args) {

        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("Players only.");
            return true;
        }

        String code = discordLinkManager.createCode(player.getUniqueId());

        player.sendMessage(Component.text("Your Discord link code: ", NamedTextColor.GREEN)
                .append(Component.text(code, NamedTextColor.YELLOW)));
        player.sendMessage(Component.text("DM the bot: !link " + code, NamedTextColor.GRAY));

        return true;
    }
}
