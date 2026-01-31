package icewizard7.miningServerPlugin.utils;

import net.kyori.adventure.text.Component;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.EventSubscription;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class NameTagManager {

    private final Plugin plugin;
    private final LuckPerms luckPerms;
    private final LegacyComponentSerializer serializer =
            LegacyComponentSerializer.legacyAmpersand();
    private EventSubscription <net.luckperms.api.event.user.UserDataRecalculateEvent> userRecalculateTask;
    private EventSubscription <net.luckperms.api.event.group.GroupDataRecalculateEvent> groupRecalculateTask;

    public NameTagManager(Plugin plugin, LuckPerms luckPerms) {
        this.plugin = plugin;
        this.luckPerms = luckPerms;
    }

    public void updateNameTag(Player player) {

        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) return;

        QueryOptions queryOptions = luckPerms.getContextManager().getQueryOptions(player);

        String prefix = user.getCachedData().getMetaData(queryOptions).getPrefix();
        String suffix = user.getCachedData().getMetaData(queryOptions).getSuffix();

        String prefixLegacy = prefix != null ? serializer.serialize(serializer.deserialize(prefix)) : "";
        String suffixLegacy = suffix != null ? serializer.serialize(serializer.deserialize(suffix)) : "";

        Component prefixComp = (prefix != null) ? serializer.deserialize(prefix) : Component.empty();
        Component suffixComp = (suffix != null) ? serializer.deserialize(suffix) : Component.empty();

        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();

        // Team name based on format, not player
        // String teamName = ("rank_" + prefixLegacy + suffixLegacy).replaceAll("[^a-zA-Z0-9]", "").substring(0, Math.min(15, ("rank_" + prefixLegacy + suffixLegacy).length()));
        String raw = prefixLegacy + suffixLegacy;
        String teamName = "rank_" + Integer.toHexString(raw.hashCode());

        Team team = board.getTeam(teamName);
        if (team == null) {
            team = board.registerNewTeam(teamName);
            team.prefix(prefixComp);
            team.suffix(suffixComp);
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
        }

        // Remove player from other rank teams
        for (Team t : board.getTeams()) {
            if (t.hasEntry(player.getName()) && !t.getName().equals(teamName)) {
                t.removeEntry(player.getName());
            }
        }

        team.addEntry(player.getName());
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

        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();

        for (Team team : board.getTeams()) {
            if (team.getName().startsWith("rank_")) {
                team.unregister();
            }
        }
    }
}
