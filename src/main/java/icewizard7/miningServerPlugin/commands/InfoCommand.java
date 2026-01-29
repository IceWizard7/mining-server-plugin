package icewizard7.miningServerPlugin.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.entity.Player;

public class InfoCommand implements CommandExecutor{
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
            return true;
        }

        if (player.hasPermission("miningServerPlugin.info")) {
            Component result = Component.text("Plugin by: ", NamedTextColor.WHITE)
                    .append(Component.text("IceWizard7", NamedTextColor.BLUE))
                    .append(Component.newline())
                    .append(Component.text("[Open-Source] ", NamedTextColor.GOLD))
                    .append(Component.text("Available on Github:", NamedTextColor.WHITE))
                    .append(Component.newline())
                    .append(Component.text("https://github.com/IceWizard7/mining-server-plugin", NamedTextColor.WHITE))
                    .clickEvent(ClickEvent.openUrl("https://github.com/IceWizard7/mining-server-plugin"))
                    .hoverEvent(HoverEvent.showText(Component.text("Click to open GitHub repository!", NamedTextColor.YELLOW)));

            player.sendMessage(result);
            return true;
        }

        player.sendMessage(Component.text(
                "You do not have permission to use this command.", NamedTextColor.RED
        ));
        return true;
    }
}
