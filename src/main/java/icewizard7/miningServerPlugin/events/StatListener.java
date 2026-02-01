package icewizard7.miningServerPlugin.events;

import icewizard7.miningServerPlugin.managers.StatManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class StatListener implements Listener {
    private final StatManager manager;

    public StatListener(StatManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller(); // null if not killed by player

        // Add death to victim
        manager.killEvent(killer, victim);
    }

    // ⛏ Block mine
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;

        manager.blockMineEvent(event.getPlayer());
    }
}
