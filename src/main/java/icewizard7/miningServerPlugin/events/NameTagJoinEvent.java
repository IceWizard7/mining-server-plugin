package icewizard7.miningServerPlugin.events;

import icewizard7.miningServerPlugin.utils.NameTagManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class NameTagJoinEvent implements Listener {

    private final NameTagManager nameTagManager;

    public NameTagJoinEvent(NameTagManager nameTagManager) {
        this.nameTagManager = nameTagManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        nameTagManager.updateNameTag(event.getPlayer());
    }
}
