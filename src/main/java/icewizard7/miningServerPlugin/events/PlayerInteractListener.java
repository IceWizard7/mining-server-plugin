package icewizard7.miningServerPlugin.events;

import icewizard7.miningServerPlugin.managers.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class PlayerInteractListener implements Listener {
    private final CombatManager combatManager;
    private final VoucherManager voucherManager;
    private final ShulkerViewManager shulkerViewManager;

    public PlayerInteractListener(
            CombatManager combatManager, VoucherManager voucherManager,
            ShulkerViewManager shulkerViewManager
    ) {
        this.combatManager = combatManager;
        this.voucherManager = voucherManager;
        this.shulkerViewManager = shulkerViewManager;
    }

    @EventHandler
    public void onElytraToggle(EntityToggleGlideEvent event) {
        combatManager.elytraToggleEvent(event);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        voucherManager.interactEvent(event);
        shulkerViewManager.interactEvent(event);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        shulkerViewManager.closeEvent(event);
    }


    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        shulkerViewManager.clickEvent(event);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        shulkerViewManager.dragEvent(event);
    }

    @EventHandler
    public void onDropEvent(PlayerDropItemEvent event) {
        shulkerViewManager.dropEvent(event);
    }

    @EventHandler
    public void onSwapEvent(PlayerSwapHandItemsEvent event) {
        shulkerViewManager.swapEvent(event);
    }
}
