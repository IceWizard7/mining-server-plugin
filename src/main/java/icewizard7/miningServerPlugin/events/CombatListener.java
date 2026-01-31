package icewizard7.miningServerPlugin.events;

import icewizard7.miningServerPlugin.managers.CombatManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class CombatListener implements Listener {
    private final CombatManager combatManager;

    public CombatListener(CombatManager combatManager) {
        this.combatManager = combatManager;
    }

    @EventHandler
    public void onCombat(EntityDamageByEntityEvent event) {
        Entity victim = event.getEntity();
        Entity attacker = event.getDamager();

        // If victim is Player -> tag
        if (victim instanceof Player player) {
            combatManager.tagPlayer(player);
        }

        // Melee
        if (attacker instanceof Player player) {
            combatManager.tagPlayer(player);
        }

        // Projectile
        if (event.getDamager() instanceof Projectile projectile) {
            if (projectile.getShooter() instanceof Player player) {
                combatManager.tagPlayer(player);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        combatManager.untagPlayer(event.getPlayer());
    }
}
