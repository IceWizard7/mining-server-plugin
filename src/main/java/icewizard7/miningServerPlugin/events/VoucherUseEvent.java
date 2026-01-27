package icewizard7.miningServerPlugin.events;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
import org.bukkit.persistence.PersistentDataType;

public class VoucherUseEvent implements Listener {
    private final LuckPerms luckPerms;
    private final NamespacedKey voucherKey;

    public VoucherUseEvent(LuckPerms luckPerms, NamespacedKey voucherKey) {
        this.luckPerms = luckPerms;
        this.voucherKey = voucherKey;
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

        // Remove one item from stack
        int amount = item.getAmount();
        if (amount > 1) {
            item.setAmount(amount - 1);
        } else {
            player.getInventory().remove(item);
        }
    }
}
