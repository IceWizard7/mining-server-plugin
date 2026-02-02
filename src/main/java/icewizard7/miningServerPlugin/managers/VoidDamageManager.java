package icewizard7.miningServerPlugin.managers;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class VoidDamageManager {
    // Below Y = 0
    public void moveEvent(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (player.getLocation().getY() < 0) {
            player.damage(1000000); // clean instant death (in gamemode survival)
        }
    }

    // Actual void damage
    public void damageEvent(EntityDamageEvent event) {
        // Only care about players
        if (!(event.getEntity() instanceof Player)) return;

        // Check if the damage cause is the void
        if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
            event.setDamage(1000000); // clean instant death (in gamemode survival)
        }
    }
}
