package icewizard7.miningServerPlugin.events;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class VoucherUseEvent implements Listener {
    private final LuckPerms luckPerms;
    private final NamespacedKey voucherKey;
    private final NamespacedKey fragmentKey;

    public VoucherUseEvent(LuckPerms luckPerms, NamespacedKey voucherKey, NamespacedKey fragmentKey) {
        this.luckPerms = luckPerms;
        this.voucherKey = voucherKey;
        this.fragmentKey = fragmentKey;
    }

    @EventHandler
    public void onVoucherUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Only right-click in main hand
        if (!event.getAction().name().contains("RIGHT_CLICK")) return;
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta()) return;

        // Check for voucher key
        if (!item.getItemMeta().getPersistentDataContainer().has(voucherKey, PersistentDataType.STRING)) return;

        String roleName = item.getItemMeta().getPersistentDataContainer().get(voucherKey, PersistentDataType.STRING);

        // Check if role exists
        if (luckPerms.getGroupManager().getGroup(roleName) == null) {
            player.sendMessage(Component.text("This voucher contains an invalid role.", NamedTextColor.RED));
            return;
        }

        // Add role to LuckPerms
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) {
            player.sendMessage(Component.text("Could not load your LuckPerms data.", NamedTextColor.RED));
            return;
        }

        InheritanceNode node = InheritanceNode.builder(roleName).build();
        user.data().add(node);
        luckPerms.getUserManager().saveUser(user);

        player.sendMessage(Component.text("You have received the role: ", NamedTextColor.GRAY)
                .append(Component.text(roleName, NamedTextColor.GOLD)));

        ItemStack itemFragment = item.clone();
        itemFragment.setAmount(1);
        ItemMeta metaFragment = itemFragment.getItemMeta();

        metaFragment.displayName(
                Component.text("Voucher Fragment: ", NamedTextColor.GREEN)
                        .append(Component.text("[", NamedTextColor.GRAY))
                        .decoration(TextDecoration.ITALIC, false)
                        .append(Component.text(roleName, NamedTextColor.GOLD)
                                .decoration(TextDecoration.ITALIC, false))
                        .append(Component.text("]", NamedTextColor.GRAY))
        );
        metaFragment.lore(List.of(
                Component.text("Fragment of rank: ", NamedTextColor.GRAY).append(Component.text(roleName, NamedTextColor.GOLD))
                        .decoration(TextDecoration.ITALIC, false)
        ));
        metaFragment.getPersistentDataContainer().remove(voucherKey);
        metaFragment.getPersistentDataContainer().set(fragmentKey, PersistentDataType.STRING, roleName);
        itemFragment.setItemMeta(metaFragment);

        // Remove one item from stack
        int amount = item.getAmount();
        if (amount > 1) {
            item.setAmount(amount - 1);
        } else {
            player.getInventory().remove(item);
            player.getInventory().addItem(itemFragment);
        }
    }
}
