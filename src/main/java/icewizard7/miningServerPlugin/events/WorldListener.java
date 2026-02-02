package icewizard7.miningServerPlugin.events;

import icewizard7.miningServerPlugin.managers.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;

public class WorldListener implements Listener {
    private final StatManager statManager;
    private final PortalManager portalManager;
    private final SpawnManager spawnManager;
    private final VoidDamageManager voidDamageManager;
    private final TelepathyManager telepathyManager;

    public WorldListener(
            StatManager statManager, PortalManager portalManager,
            SpawnManager spawnManager, VoidDamageManager voidDamageManager,
            TelepathyManager telepathyManager
    ) {
        this.statManager = statManager;
        this.portalManager = portalManager;
        this.spawnManager = spawnManager;
        this.voidDamageManager = voidDamageManager;
        this.telepathyManager = telepathyManager;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        portalManager.moveEvent(event);
        voidDamageManager.moveEvent(event);
    }

    @EventHandler
    public void onBedEnter(PlayerBedEnterEvent event) {
        spawnManager.bedEnterEvent(event);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        statManager.blockMineEvent(event);
        telepathyManager.blockMineEvent(event);
    }
}
