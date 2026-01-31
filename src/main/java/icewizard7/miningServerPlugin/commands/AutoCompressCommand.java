package icewizard7.miningServerPlugin.commands;

import icewizard7.miningServerPlugin.managers.AutoCompressManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class AutoCompressCommand implements CommandExecutor {
    private final AutoCompressManager autoCompressManager;

    public AutoCompressCommand(AutoCompressManager autoCompressManager) {
        this.autoCompressManager = autoCompressManager;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
            return true;
        }

        return autoCompressManager.autoCompress(player);
    }
}
