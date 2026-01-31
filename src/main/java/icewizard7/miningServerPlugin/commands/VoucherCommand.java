package icewizard7.miningServerPlugin.commands;

import icewizard7.miningServerPlugin.managers.VoucherManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.luckperms.api.LuckPerms;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class VoucherCommand implements CommandExecutor {
    private final LuckPerms luckPerms;
    private final VoucherManager voucherManager;

    public VoucherCommand(LuckPerms luckPerms, VoucherManager voucherManager) {
        this.luckPerms = luckPerms;
        this.voucherManager = voucherManager;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
            return true;
        }

        if (!player.hasPermission("miningServerPlugin.voucher.create")) {
            player.sendMessage(Component.text("You do not have permission to create vouchers.", NamedTextColor.RED));
            return true;
        }

        if (args.length != 2) {
            player.sendMessage(Component.text("Usage: /voucher create <rank>", NamedTextColor.RED));
            return true;
        }

        String rankName = args[1];

        // Check if rank exists in LuckPerms
        if (luckPerms.getGroupManager().getGroup(rankName) == null) {
            player.sendMessage("The rank '" + rankName + "' does not exist!");
            return true;
        }

        // Create voucher item
        ItemStack voucher = new ItemStack(Material.PAPER, 1);
        ItemMeta meta = voucher.getItemMeta();
        meta.displayName(
                Component.text("Voucher: ", NamedTextColor.GREEN)
                        .append(Component.text("[", NamedTextColor.GRAY))
                        .decoration(TextDecoration.ITALIC, false)
                        .append(Component.text(rankName, NamedTextColor.GOLD)
                                .decoration(TextDecoration.ITALIC, false))
                        .append(Component.text("]", NamedTextColor.GRAY))
        );
        meta.lore(List.of(
                Component.text("Right-click to receive the rank: ", NamedTextColor.GRAY).append(Component.text(rankName, NamedTextColor.GOLD))
                        .decoration(TextDecoration.ITALIC, false)
        ));
        meta.getPersistentDataContainer().set(voucherManager.getVoucherKey(), PersistentDataType.STRING, rankName);
        voucher.setItemMeta(meta);

        player.getInventory().addItem(voucher);
        player.sendMessage("Voucher for rank '" + rankName + "' has been added to your inventory!");

        return true;
    }
}
