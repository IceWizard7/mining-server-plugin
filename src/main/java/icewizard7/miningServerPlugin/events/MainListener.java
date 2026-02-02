package icewizard7.miningServerPlugin.events;

import icewizard7.miningServerPlugin.managers.*;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class MainListener implements Listener {
    private final ChatManager chatManager;
    private final TabManager tabManager;
    private final StatManager statManager;
    private final JoinQuitMessageManager joinQuitMessageManager;
    private final VanishManager vanishManager;
    private final CombatManager combatManager;
    private final NameTagManager nameTagManager;
    private final PortalManager portalManager;
    private final SpawnManager spawnManager;
    private final VoucherManager voucherManager;
    private final VoidDamageManager voidDamageManager;
    private final TelepathyManager telepathyManager;

    public MainListener(
            ChatManager chatManager, TabManager tabManager,
            StatManager statManager, JoinQuitMessageManager joinQuitMessageManager,
            VanishManager vanishManager, CombatManager combatManager,
            NameTagManager nameTagManager, PortalManager portalManager,
            SpawnManager spawnManager, VoucherManager voucherManager,
            VoidDamageManager voidDamageManager, TelepathyManager telepathyManager
    ) {
        this.chatManager = chatManager;
        this.tabManager = tabManager;
        this.statManager = statManager;
        this.joinQuitMessageManager = joinQuitMessageManager;
        this.vanishManager = vanishManager;
        this.combatManager = combatManager;
        this.nameTagManager = nameTagManager;
        this.portalManager = portalManager;
        this.spawnManager = spawnManager;
        this.voucherManager = voucherManager;
        this.voidDamageManager = voidDamageManager;
        this.telepathyManager = telepathyManager;
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        chatManager.chatEvent(event);
    }

    @EventHandler
    public void onCombat(EntityDamageByEntityEvent event) {
        combatManager.combatEvent(event);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        combatManager.deathEvent(event);
        statManager.deathEvent(event);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        tabManager.joinEvent(event);
        statManager.joinEvent(event);
        nameTagManager.joinEvent(event);
        vanishManager.joinEvent(event);
        spawnManager.joinEvent(event);
        joinQuitMessageManager.joinEvent(event);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        combatManager.quitEvent(event);
        nameTagManager.quitEvent(event);
        vanishManager.quitEvent(event);
        statManager.quitEvent(event);
        joinQuitMessageManager.quitEvent(event);
    }

    @EventHandler
    public void onElytraToggle(EntityToggleGlideEvent event) {
        combatManager.elytraToggleEvent(event);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        portalManager.moveEvent(event);
        voidDamageManager.moveEvent(event);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        spawnManager.respawnEvent(event);
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

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        voucherManager.interactEvent(event);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        voidDamageManager.damageEvent(event);
    }
}
