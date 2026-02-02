package icewizard7.miningServerPlugin.commands;

import icewizard7.miningServerPlugin.managers.CombatManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FlySpeedCommand implements CommandExecutor {
    private final CombatManager combatManager;

    public FlySpeedCommand(CombatManager combatManager) {
        this.combatManager = combatManager;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
            return true;
        }

        if (!(player.hasPermission("miningServerPlugin.fly"))) {
            player.sendMessage(Component.text(
                    "You do not have permission to use this command.", NamedTextColor.RED
            ));
            return true;
        }

        if (combatManager.isInCombat(player)) {
            combatManager.sendCombatMessage(player);
            return true;
        }

        if (args.length == 0) {
            float currentSpeed = player.getFlySpeed() * 10;
            Component flySpeedMessage = Component.newline().append(Component.text("Current speed: " + currentSpeed));

            Component finalMessage = Component.text("Usage: /flyspeed <speed>", NamedTextColor.RED);

            if (player.isFlying()) {
                finalMessage = finalMessage.append(flySpeedMessage);
            }

            player.sendMessage(finalMessage);
            return true;
        }

        int rawSpeed;

        try {
            rawSpeed = Integer.parseInt(args[0]);
        } catch(NumberFormatException e) {
            player.sendMessage(Component.text(
                    "Usage: /flyspeed <speed>", NamedTextColor.RED
            ));
            return true;
        }

        rawSpeed = Math.min(rawSpeed, 10);
        rawSpeed = Math.max(rawSpeed, -10);

        float finalSpeed = (float) rawSpeed / 10;

        player.setFlySpeed(finalSpeed);

        player.sendMessage(Component.text(
                "Updated flyspeed to " + rawSpeed + ".", NamedTextColor.GREEN
        ));

        return true;
    }
}
