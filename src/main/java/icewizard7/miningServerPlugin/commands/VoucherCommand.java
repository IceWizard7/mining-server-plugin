package icewizard7.miningServerPlugin.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.luckperms.api.LuckPerms;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class VoucherCommand implements CommandExecutor {
    private final LuckPerms luckPerms;
    private final NamespacedKey voucherKey;

    public VoucherCommand(LuckPerms luckPerms, Plugin plugin) {
        this.luckPerms = luckPerms;
        this.voucherKey = new NamespacedKey(plugin, "voucher");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
            return true;
        }

        if (!player.hasPermission("miningServerPlugin.voucher.create")) {
            player.sendMessage(Component.text("You do not have permission to create vouchers.", NamedTextColor.RED));
            return true;
        }

        if (args.length != 2) {
            player.sendMessage(Component.text("Usage: /voucher create <role>", NamedTextColor.RED));
            return true;
        }

        String roleName = args[1];

        // Check if role exists in LuckPerms
        if (luckPerms.getGroupManager().getGroup(roleName) == null) {
            player.sendMessage("The role '" + roleName + "' does not exist!");
            return true;
        }

        // Create voucher item
        ItemStack voucher = new ItemStack(Material.PAPER, 1);
        ItemMeta meta = voucher.getItemMeta();
        meta.displayName(
                Component.text("Voucher: ", NamedTextColor.GREEN)
                        .append(Component.text("[", NamedTextColor.GRAY))
                        .decoration(TextDecoration.ITALIC, false)
                        .append(Component.text(roleName, NamedTextColor.GOLD)
                                .decoration(TextDecoration.ITALIC, false))
                        .append(Component.text("]", NamedTextColor.GRAY))
        );
        meta.lore(List.of(
                Component.text("Right-click to receive the rank: ", NamedTextColor.GRAY).append(Component.text(roleName, NamedTextColor.GOLD))
                        .decoration(TextDecoration.ITALIC, false)
        ));
        meta.getPersistentDataContainer().set(voucherKey, PersistentDataType.STRING, roleName);
        voucher.setItemMeta(meta);

        player.getInventory().addItem(voucher);
        player.sendMessage("Voucher for role '" + roleName + "' has been added to your inventory!");

        return true;
    }

    public NamespacedKey getVoucherKey() {
        return voucherKey;
    }
}
