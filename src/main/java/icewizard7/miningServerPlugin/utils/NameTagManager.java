package icewizard7.miningServerPlugin.utils;

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

        String prefixLegacy = prefix != null
                ? serializer.serialize(serializer.deserialize(prefix))
                : "";

        String suffixLegacy = suffix != null
                ? serializer.serialize(serializer.deserialize(suffix))
                : "";

        // Use main scoreboard
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();

        // Unique team per rank format
        String teamName = "rank_" + player.getUniqueId().toString().substring(0, 8);

        Team team = board.getTeam(teamName);
        if (team == null) {
            team = board.registerNewTeam(teamName);
        }

        team.prefix(net.kyori.adventure.text.Component.text(prefixLegacy));
        team.suffix(net.kyori.adventure.text.Component.text(suffixLegacy));

        if (!team.hasEntry(player.getName())) {
            team.addEntry(player.getName());
        }

        player.setScoreboard(board);
    }
}
