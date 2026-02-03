package icewizard7.miningServerPlugin.managers;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

public class ShulkerViewManager {
    public void interactEvent(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Only right-click in main hand
        if (!event.getAction().name().contains("RIGHT_CLICK")) return;
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;

        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null) return;

        Material type = item.getType();
        if (!type.name().endsWith("SHULKER_BOX")) return;

        event.setCancelled(true);

        openShulkerPreview(player, item);
    }

    public void closeEvent(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        if (!event.getView().title().equals(Component.text("Shulker Preview"))) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null) return;

        if (!(item.getItemMeta() instanceof BlockStateMeta meta)) return;
        if (!(meta.getBlockState() instanceof ShulkerBox shulker)) return;

        // Save contents
        shulker.getInventory().setContents(event.getInventory().getContents());

        meta.setBlockState(shulker);
        item.setItemMeta(meta);
    }

    private void openShulkerPreview(Player player, ItemStack shulkerItem) {

        if (!(shulkerItem.getItemMeta() instanceof BlockStateMeta meta)) return;
        if (!(meta.getBlockState() instanceof ShulkerBox shulker)) return;

        Inventory inv = Bukkit.createInventory(player, 27, Component.text("Shulker Preview"));
        inv.setContents(shulker.getInventory().getContents());

        player.openInventory(inv);
    }
}
