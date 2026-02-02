package icewizard7.miningServerPlugin.commands;

import icewizard7.miningServerPlugin.managers.VoucherManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VoucherCommand implements CommandExecutor {
    private final VoucherManager voucherManager;

    public VoucherCommand(VoucherManager voucherManager) {
        this.voucherManager = voucherManager;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
            return true;
        }

        return voucherManager.createVoucher(player, args);
    }
}
