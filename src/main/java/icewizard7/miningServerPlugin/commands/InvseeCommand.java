package icewizard7.miningServerPlugin.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class InvseeCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(Component.text(
                    "Only players can execute this command.", NamedTextColor.RED
            ));
            return true;
        }

        Player player = (Player) commandSender;

        if (!(player.hasPermission("miningServerPlugin.invsee"))) {
            player.sendMessage(Component.text(
                    "You do not have permission to use this command.", NamedTextColor.RED
            ));
            return true;
        }

        if (strings.length == 0) {
            player.sendMessage(Component.text(
                    "Insert a player.", NamedTextColor.RED
            ));
            return true;
        }

        Player target = Bukkit.getPlayerExact(strings[0]);

        if (target == player) {
            player.sendMessage(Component.text(
                    "You can't open your own inventory.", NamedTextColor.RED
            ));
            return true;
        }

        if (target == null) {
            player.sendMessage(Component.text(
                    "Player was not found.", NamedTextColor.RED
            ));
            return true;
        }

        Inventory targetInventory = target.getInventory();

        player.openInventory(targetInventory);
        player.sendMessage(Component.text(
                "You are opening" + target.getName() + "'s inventory.", NamedTextColor.GREEN
        ));

        return true;
    }
}
