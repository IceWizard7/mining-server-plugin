package icewizard7.miningServerPlugin.events;

import icewizard7.miningServerPlugin.managers.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class DamageListener implements Listener {
    private final CombatManager combatManager;
    private final VoidDamageManager voidDamageManager;

    public DamageListener(
            CombatManager combatManager, VoidDamageManager voidDamageManager
    ) {
        this.combatManager = combatManager;
        this.voidDamageManager = voidDamageManager;
    }

    @EventHandler
    public void onCombat(EntityDamageByEntityEvent event) {
        combatManager.combatEvent(event);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        voidDamageManager.damageEvent(event);
    }
}
