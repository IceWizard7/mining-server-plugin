package icewizard7.miningServerPlugin.managers;

import net.kyori.adventure.text.Component;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.EventSubscription;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.UUID;

public class NameTagManager {

    private final Plugin plugin;
    private final LuckPerms luckPerms;
    private final LuckPermsManager luckPermsManager;
    private final StatManager statManager;
    private EventSubscription <net.luckperms.api.event.user.UserDataRecalculateEvent> userRecalculateTask;
    private EventSubscription <net.luckperms.api.event.group.GroupDataRecalculateEvent> groupRecalculateTask;

    public NameTagManager(Plugin plugin, LuckPerms luckPerms, LuckPermsManager luckPermsManager, StatManager statManager) {
        this.plugin = plugin;
        this.luckPerms = luckPerms;
        this.luckPermsManager = luckPermsManager;
        this.statManager = statManager;
    }

    public void updateNameTag(Player player) {

        User user = luckPermsManager.getOrLoadUser(player.getUniqueId());
        if (user == null) return;

        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();

        // Team name based on format, not player
        UUID uuid = player.getUniqueId();
        String raw = luckPermsManager.getStringPrefix(uuid) + luckPermsManager.getStringSuffix(uuid);
        String teamName = "rank_" + Integer.toHexString(raw.hashCode());

        Team team = board.getTeam(teamName);
        if (team == null) {
            team = board.registerNewTeam(teamName);
            team.prefix(luckPermsManager.getComponentPrefix(uuid));
            team.suffix(luckPermsManager.getComponentSuffix(uuid));
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
        }

        // Remove player from other rank teams
        for (Team t : board.getTeams()) {
            if (t.hasEntry(player.getName()) && !t.getName().equals(teamName)) {
                t.removeEntry(player.getName());
            }
        }

        team.addEntry(player.getName());
        addEntryToAllScoreboards(teamName, player.getName(),
                luckPermsManager.getComponentPrefix(uuid),
                luckPermsManager.getComponentSuffix(uuid));
    }

    private void addEntryToAllScoreboards(String teamName, String entry, Component prefix, Component suffix) {
        for (Scoreboard sb : statManager.getScoreBoards().values()) {
            Team team = sb.getTeam(teamName);
            if (team == null) {
                team = sb.registerNewTeam(teamName);
                team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
            }
            team.prefix(prefix);
            team.suffix(suffix);
            if (!team.hasEntry(entry)) team.addEntry(entry);
        }
    }

    public void startNameTagTask() {
        // User recalculate
        this.userRecalculateTask = luckPerms.getEventBus().subscribe(plugin,
                net.luckperms.api.event.user.UserDataRecalculateEvent.class,
                event -> Bukkit.getScheduler().runTask(plugin, () -> {
                    Player player = Bukkit.getPlayer(event.getUser().getUniqueId());
                    if (player != null) {
                        updateNameTag(player);
                    }
                })
        );

        // Group recalculate
        this.groupRecalculateTask = luckPerms.getEventBus().subscribe(plugin,
                net.luckperms.api.event.group.GroupDataRecalculateEvent.class,
                event -> Bukkit.getScheduler().runTask(plugin, () -> {

                    String groupName = event.getGroup().getName();

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
                        if (user == null) continue;

                        if (user.getInheritedGroups(user.getQueryOptions()).stream()
                                .anyMatch(g -> g.getName().equalsIgnoreCase(groupName))) {

                            updateNameTag(player);
                        }
                    }
                })
        );
    }

    public void shutdown() {
        if (userRecalculateTask != null) {
            userRecalculateTask.close();
            userRecalculateTask = null;
        }

        if (groupRecalculateTask != null) {
            groupRecalculateTask.close();
            groupRecalculateTask = null;
        }

        for (Scoreboard sb : statManager.getScoreBoards().values()) {
            for (Team t : sb.getTeams()) {
                if (t.getName().startsWith("rank_")) t.unregister();
            }
        }

        Scoreboard main = Bukkit.getScoreboardManager().getMainScoreboard();
        for (Team t : main.getTeams()) {
            if (t.getName().startsWith("rank_")) t.unregister();
        }
    }
}
