package icewizard7.miningServerPlugin.events;

import icewizard7.miningServerPlugin.managers.CombatManager;
import icewizard7.miningServerPlugin.managers.WorldGuardManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.entity.Player;

public class ElytraListener implements Listener {

    private final CombatManager combatManager;
    private final WorldGuardManager worldGuardManager;

    public ElytraListener(CombatManager combatManager, WorldGuardManager worldGuardManager) {
        this.combatManager = combatManager;
        this.worldGuardManager = worldGuardManager;
    }

    @EventHandler
    public void onElytraToggle(EntityToggleGlideEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (!event.isGliding()) {
            return;
        }

        // Player is starting to glide

        // Check if player is in combat
        if (combatManager.isInCombat(player) && !worldGuardManager.isInRegion(player, "pvp")) {
            player.sendMessage(Component.text("You cannot use Elytra while in combat!", NamedTextColor.RED));
            event.setCancelled(true); // Blocks Elytra flight
        }
    }
}
