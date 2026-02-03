package icewizard7.miningServerPlugin.events;

import icewizard7.miningServerPlugin.managers.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;

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
}
