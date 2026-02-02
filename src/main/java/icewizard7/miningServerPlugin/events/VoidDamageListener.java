package icewizard7.miningServerPlugin.events;

import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

public class VoidDamageListener implements Listener {
    // Below Y = 0
    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (player.getLocation().getY() < 0) {
            player.damage(1000000); // clean instant death (in gamemode survival)
        }
    }

    // Actual void damage
    @EventHandler
    public void onVoidDamage(EntityDamageEvent event) {
        // Only care about players
        if (!(event.getEntity() instanceof Player)) return;

        // Check if the damage cause is the void
        if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
            event.setDamage(1000000); // clean instant death (in gamemode survival)
        }
    }
}
