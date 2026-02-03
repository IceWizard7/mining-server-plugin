package icewizard7.miningServerPlugin.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Tag;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShulkerViewManager {

    private final Map<UUID, Integer> openSlots = new HashMap<>();
    private final Component GUI_TITLE = Component.text("Shulker Preview");

    public void interactEvent(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Only right-click in main hand
        if (!event.getAction().name().contains("RIGHT_CLICK")) return;
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || !isShulker(item)) return;

        // Cancel the placement of the block
        event.setCancelled(true);

        openShulkerPreview(player, item);
    }

    public void closeEvent(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (!event.getView().title().equals(GUI_TITLE)) return;

        Integer slot = openSlots.remove(player.getUniqueId());
        if (slot == null) return;

        ItemStack item = player.getInventory().getItem(slot);

        // SECURITY: If item is missing/swapped, we must warn or handle it.
        // With the protections below, this should theoretically not happen,
        // but if it does, the changes in the viewer are lost (safest fallback).
        if (item == null || !isShulker(item)) {
            player.sendMessage(Component.text("Do not attempt to dupe glitch. It can result in a ban.", NamedTextColor.RED));
            return;
        }

        if (!(item.getItemMeta() instanceof BlockStateMeta meta)) return;
        if (!(meta.getBlockState() instanceof ShulkerBox shulker)) return;

        // Save contents
        shulker.getInventory().setContents(event.getInventory().getContents());
        meta.setBlockState(shulker);
        item.setItemMeta(meta);
    }

    public void clickEvent(InventoryClickEvent event) {
        InventoryView view = event.getView();
        if (!view.title().equals(GUI_TITLE)) return;

        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        // Prevent nesting shulkers (Cursor / Click)
        if (isShulker(cursor) || isShulker(current)) {
            event.setCancelled(true);
            return;
        }

        // Prevent Hotbar-Swapping a Shulker INTO the GUI
        if (event.getClick() == ClickType.NUMBER_KEY) {
            ItemStack targetHotbarItem = player.getInventory().getItem(event.getHotbarButton());
            if (isShulker(targetHotbarItem)) {
                event.setCancelled(true);
                return;
            }
        }

        // Lockdown Logic
        Integer lockedSlot = openSlots.get(player.getUniqueId());
        if (lockedSlot != null) {

            // Prevent moving the locked item directly (Clicking the slot)
            if (event.getClickedInventory() == player.getInventory() && event.getSlot() == lockedSlot) {
                event.setCancelled(true);
                return;
            }

            // Prevent swapping WITH the locked item (Pressing 1-9 while hovering over GUI)
            // If the hotkey pressed corresponds to the locked slot index
            if (event.getClick() == ClickType.NUMBER_KEY && event.getHotbarButton() == lockedSlot) {
                event.setCancelled(true);
                return;
            }

            // Prevent Offhand Swapping (F key) inside GUI
            if (event.getClick() == ClickType.SWAP_OFFHAND) {
                event.setCancelled(true);
                return;
            }

            // Prevent "Double Click" collection grabbing the locked item
            // If the user double clicks a shulker box item in the GUI, it might pull the locked one.
            if (event.getClick() == ClickType.DOUBLE_CLICK) {
                // Simplest fix: Just deny double clicks on Shulker Boxes specifically
                if (isShulker(event.getCursor()) || isShulker(event.getCurrentItem())) {
                    event.setCancelled(true);
                    return;
                }
            }

            // Prevent "Drop" key (Q) on the locked slot
            if ((event.getClick() == ClickType.DROP || event.getClick() == ClickType.CONTROL_DROP)
                    && event.getClickedInventory() == player.getInventory()
                    && event.getSlot() == lockedSlot) {
                event.setCancelled(true);
                return;
            }
        }
    }

    // Handles Dragging items across the inventory
    public void dragEvent(InventoryDragEvent event) {
        if (!event.getView().title().equals(GUI_TITLE)) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Integer lockedSlot = openSlots.get(player.getUniqueId());
        if (lockedSlot != null) {

            // Prevent dragging a Shulker Box at all (Safest)
            if (isShulker(event.getOldCursor())) {
                event.setCancelled(true);
                return;
            }

            // Prevent dragging items INTO the locked slot
            // We must compare Raw Slots because drag events use Raw Slots
            int topSize = event.getView().getTopInventory().getSize(); // Usually 27 for this GUI

            // The raw slot ID for the player's hotbar slot:
            // TopInvSize + 27 (Main Inv rows) + lockedSlot
            int rawLockedSlot = topSize + 27 + lockedSlot;

            if (event.getRawSlots().contains(rawLockedSlot)) {
                event.setCancelled(true);
            }
        }
    }

    public void dropEvent(PlayerDropItemEvent event) {
        // Prevent dropping items while inventory is open (outside GUI handling)
        if (openSlots.containsKey(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    // Prevent 'F' key swap
    public void swapEvent(PlayerSwapHandItemsEvent event) {
        if (openSlots.containsKey(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    private boolean isShulker(ItemStack item) {
        if (item == null) return false;
        return Tag.SHULKER_BOXES.isTagged(item.getType());
    }

    private void openShulkerPreview(Player player, ItemStack shulkerItem) {
        if (!(shulkerItem.getItemMeta() instanceof BlockStateMeta meta)) return;
        if (!(meta.getBlockState() instanceof ShulkerBox shulker)) return;

        openSlots.put(player.getUniqueId(), player.getInventory().getHeldItemSlot());

        Inventory inv = Bukkit.createInventory(player, 27, GUI_TITLE);
        inv.setContents(shulker.getInventory().getContents());

        player.openInventory(inv);
    }
}