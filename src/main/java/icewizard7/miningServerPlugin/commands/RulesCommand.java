package icewizard7.miningServerPlugin.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public class RulesCommand implements CommandExecutor{
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
            return true;
        }

        if (!(player.hasPermission("miningServerPlugin.rules"))) {
            player.sendMessage(Component.text(
                    "You do not have permission to use this command.", NamedTextColor.RED
            ));
            return true;
        }

        Component result = Component.text("Minor Offences:", NamedTextColor.GOLD)
                        .append(Component.newline())
                        .append(Component.text("1: ", NamedTextColor.RED))
                        .append(Component.text("spamming", NamedTextColor.WHITE))
                        .append(Component.newline())
                        .append(Component.text("2: ", NamedTextColor.RED))
                        .append(Component.text("promoting / advertising (ex. other servers)", NamedTextColor.WHITE))
                        .append(Component.newline())
                        .append(Component.text("3: ", NamedTextColor.RED))
                        .append(Component.text("being toxic / harassing", NamedTextColor.WHITE))
                        .append(Component.newline())
                        .append(Component.text("4: ", NamedTextColor.RED))
                        .append(Component.text("minor bug abuse", NamedTextColor.WHITE))
                        .append(Component.newline())
                        .append(Component.text("5: ", NamedTextColor.RED))
                        .append(Component.text("killing people without armor (if they own armor, but are not wearing it, there is no punishment)", NamedTextColor.WHITE))
                        .append(Component.newline())
                        .append(Component.text("6: ", NamedTextColor.RED))
                        .append(Component.text("AFK killing", NamedTextColor.WHITE))
                        .append(Component.newline())
                        .append(Component.text("7: ", NamedTextColor.RED))
                        .append(Component.text("spawn killing", NamedTextColor.WHITE))
                        .append(Component.newline())
                        .append(Component.newline())
                        .append(Component.text("Major Offences:", NamedTextColor.GOLD))
                        .append(Component.newline())
                        .append(Component.text("1: ", NamedTextColor.RED))
                        .append(Component.text("hacking", NamedTextColor.WHITE))
                        .append(Component.newline())
                        .append(Component.text("2: ", NamedTextColor.RED))
                        .append(Component.text("doxxing", NamedTextColor.WHITE))
                        .append(Component.newline())
                        .append(Component.text("3: ", NamedTextColor.RED))
                        .append(Component.text("major bug abuse (ex. duping)", NamedTextColor.WHITE))
                        .append(Component.newline())
                        .append(Component.text("4: ", NamedTextColor.RED))
                        .append(Component.text("stealing / scamming", NamedTextColor.WHITE))
                        .append(Component.newline())
                        .append(Component.newline())
                        .append(Component.text("If you break any rule, your punishment will be decided by determining its severity (by staff members).", NamedTextColor.WHITE));

        player.sendMessage(result);
        return true;
    }
}
