package icewizard7.miningServerPlugin.events;

import icewizard7.miningServerPlugin.managers.StatManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class StatListener implements Listener {
    private final StatManager statManager;

    public StatListener(StatManager statManager) {
        this.statManager = statManager;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller(); // null if not killed by player

        // Add death to victim
        statManager.killEvent(killer, victim);
    }

    // Block mine
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;

        statManager.blockMineEvent(event.getPlayer());
    }
}
