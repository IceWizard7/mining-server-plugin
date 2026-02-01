package icewizard7.miningServerPlugin.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.entity.Player;

public class VoidDamageListener implements Listener {
    @EventHandler
    public void onVoidDamage(EntityDamageEvent event) {
        // Only care about players
        if (!(event.getEntity() instanceof Player)) return;

        // Check if the damage cause is the void
        if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
            event.setDamage(1000000); // clean instant death
        }
    }
}
