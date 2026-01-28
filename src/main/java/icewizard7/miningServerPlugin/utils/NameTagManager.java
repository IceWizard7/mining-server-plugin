package icewizard7.miningServerPlugin.utils;

import net.kyori.adventure.text.Component;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class NameTagManager {

    private final LuckPerms luckPerms;
    private final LegacyComponentSerializer serializer =
            LegacyComponentSerializer.legacyAmpersand();

    public NameTagManager(LuckPerms luckPerms) {
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
}
