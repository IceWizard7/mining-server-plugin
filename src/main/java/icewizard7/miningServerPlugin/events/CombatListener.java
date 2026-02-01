package icewizard7.miningServerPlugin.events;

import icewizard7.miningServerPlugin.managers.CombatManager;
import icewizard7.miningServerPlugin.managers.WorldGuardManager;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class CombatListener implements Listener {
    private final CombatManager combatManager;
    private final WorldGuardManager worldGuardManager;

    public CombatListener(CombatManager combatManager, WorldGuardManager worldGuardManager) {
        this.combatManager = combatManager;
        this.worldGuardManager = worldGuardManager;
    }

    @EventHandler
    public void onCombat(EntityDamageByEntityEvent event) {
        Entity attacker = event.getDamager();
        Entity victim = event.getEntity();

        // If you are in a region (ex. Spawn), where PvP isn't even allowed; Just don't tag anything.
        if (!worldGuardManager.inPvPAllowedRegion(victim)) return;

        // If victim is Player -> tag
        if (victim instanceof Player player) {
            combatManager.tagPlayer(player);
        }

        // Melee
        if (attacker instanceof Player player) {
            combatManager.tagPlayer(player);
        }

        // Projectile
        if (attacker instanceof Projectile projectile) {
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
