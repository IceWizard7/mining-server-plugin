package icewizard7.miningServerPlugin.events;

import icewizard7.miningServerPlugin.utils.NameTagManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class NameTagEvent implements Listener {

    private final NameTagManager nameTagManager;

    public NameTagEvent(NameTagManager nameTagManager) {
        this.nameTagManager = nameTagManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        nameTagManager.updateNameTag(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        for (Team team : board.getTeams()) {
            team.removeEntry(event.getPlayer().getName());
        }
    }

}
