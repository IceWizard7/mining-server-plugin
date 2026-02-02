package icewizard7.miningServerPlugin.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class VoucherManager {
    private final LuckPerms luckPerms;
    private final NamespacedKey voucherKey;
    private final NamespacedKey fragmentKey;

    public VoucherManager(LuckPerms luckPerms, Plugin plugin) {
        this.luckPerms = luckPerms;
        this.voucherKey = new NamespacedKey(plugin, "voucher");
        this.fragmentKey = new NamespacedKey(plugin, "fragment");
    }

    private NamespacedKey getVoucherKey() {
        return voucherKey;
    }

    private NamespacedKey getFragmentKey() {
        return fragmentKey;
    }

    public void interactEvent(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Only right-click in main hand
        if (!event.getAction().name().contains("RIGHT_CLICK")) return;
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta()) return;

        // Check for voucher key
        if (!item.getItemMeta().getPersistentDataContainer().has(getVoucherKey(), PersistentDataType.STRING)) return;

        String rankName = item.getItemMeta().getPersistentDataContainer().get(getVoucherKey(), PersistentDataType.STRING);

        // Check if rank exists
        if (luckPerms.getGroupManager().getGroup(rankName) == null) {
            player.sendMessage(Component.text("This voucher contains an invalid rank.", NamedTextColor.RED));
            return;
        }

        // Add rank to LuckPerms
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) {
            player.sendMessage(Component.text("Could not load your LuckPerms data.", NamedTextColor.RED));
            return;
        }

        if (event.useItemInHand() == org.bukkit.event.Event.Result.DENY) return;
        event.setCancelled(true);

        InheritanceNode node = InheritanceNode.builder(rankName).build();
        user.data().add(node);
        luckPerms.getUserManager().saveUser(user);

        player.sendMessage(Component.text("You have received the rank: ", NamedTextColor.GRAY)
                .append(Component.text(rankName, NamedTextColor.GOLD)));

        ItemStack itemFragment = item.clone();
        itemFragment.setAmount(1);
        ItemMeta metaFragment = itemFragment.getItemMeta();

        metaFragment.displayName(
                Component.text("Voucher Fragment: ", NamedTextColor.GREEN)
                        .append(Component.text("[", NamedTextColor.GRAY))
                        .decoration(TextDecoration.ITALIC, false)
                        .append(Component.text(rankName, NamedTextColor.GOLD)
                                .decoration(TextDecoration.ITALIC, false))
                        .append(Component.text("]", NamedTextColor.GRAY))
        );
        metaFragment.lore(List.of(
                Component.text("Fragment of rank: ", NamedTextColor.GRAY).append(Component.text(rankName, NamedTextColor.GOLD))
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("Obtained by redeeming rank ", NamedTextColor.DARK_GRAY).append(Component.text(rankName, NamedTextColor.GOLD))
                        .decoration(TextDecoration.ITALIC, false)
        ));
        metaFragment.getPersistentDataContainer().remove(getVoucherKey());
        metaFragment.getPersistentDataContainer().set(getFragmentKey(), PersistentDataType.STRING, rankName);
        itemFragment.setItemMeta(metaFragment);

        player.getInventory().addItem(itemFragment);
        // Remove one item from stack
        int amount = item.getAmount();
        if (amount > 1) {
            item.setAmount(amount - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
    }

    public boolean createVoucher(Player player, String[] args) {
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
        meta.getPersistentDataContainer().set(getVoucherKey(), PersistentDataType.STRING, rankName);
        voucher.setItemMeta(meta);

        player.getInventory().addItem(voucher);
        player.sendMessage("Voucher for rank '" + rankName + "' has been added to your inventory!");

        return true;
    }
}
