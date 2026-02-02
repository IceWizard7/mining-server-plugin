package icewizard7.miningServerPlugin.managers;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.EventSubscription;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
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

    private EventSubscription<net.luckperms.api.event.user.UserDataRecalculateEvent> userRecalculateTask;
    private EventSubscription<net.luckperms.api.event.group.GroupDataRecalculateEvent> groupRecalculateTask;

    public NameTagManager(Plugin plugin, LuckPerms luckPerms, LuckPermsManager luckPermsManager, StatManager statManager) {
        this.plugin = plugin;
        this.luckPerms = luckPerms;
        this.luckPermsManager = luckPermsManager;
        this.statManager = statManager;

        // Link back to StatManager
        this.statManager.setNameTagManager(this);
    }

    /**
     * Called when StatManager creates a BRAND NEW scoreboard for a player.
     * We must populate this empty board with the tags of everyone else currently online.
     */
    public void initNewBoard(Scoreboard board) {
        for (Player target : Bukkit.getOnlinePlayers()) {
            applyTagToBoard(target, board);
        }
    }

    /**
     * Updates a specific player's nametag across ALL active scoreboards.
     * Called when a player joins or rank changes.
     */
    public void updateNameTag(Player player) {
        // Update on every active scoreboard (every other player's view)
        for (Scoreboard board : statManager.getScoreBoards().values()) {
            applyTagToBoard(player, board);
        }
    }

    /**
     * The logic to calculate rank, create the team, and add the player
     * on a SPECIFIC scoreboard.
     */
    private void applyTagToBoard(Player player, Scoreboard board) {
        UUID uuid = player.getUniqueId();
        User user = luckPerms.getUserManager().getUser(uuid);
        if (user == null) return;

        // Calculate Sorting Weight (Higher weight = Higher in TabList)
        // We inverse it (9999 - weight) because Scoreboards sort A-Z (Ascending)
        int weight = user.getCachedData().getMetaData(QueryOptions.defaultContextualOptions()).getWeight();
        String order = String.format("%04d", 9999 - weight);

        // generate unique ID for this prefix setup
        // We group players with the exact same prefix into the same team to save resources
        String prefixStr = luckPermsManager.getStringPrefix(uuid);
        String suffixStr = luckPermsManager.getStringSuffix(uuid);
        String uniqueId = Integer.toHexString((prefixStr + suffixStr).hashCode());

        // Team Name:  "0001_rankHash" -> Ensures sorting + uniqueness
        String teamName = order + "_" + uniqueId;

        // Get or Create Team
        Team team = board.getTeam(teamName);
        if (team == null) {
            team = board.registerNewTeam(teamName);
            team.prefix(luckPermsManager.getComponentPrefix(uuid));
            team.suffix(luckPermsManager.getComponentSuffix(uuid));
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
        }

        // Clean up: Remove player from old teams on THIS board
        // If the player ranked up, they might be in "0002_Member", we need to move them to "0001_Admin"
        for (Team t : board.getTeams()) {
            if (t.hasEntry(player.getName()) && !t.getName().equals(teamName)) {
                t.removeEntry(player.getName());
            }
        }

        // Add if not present
        if (!team.hasEntry(player.getName())) {
            team.addEntry(player.getName());
        }
    }

    public void quitEvent(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        for (Team team : board.getTeams()) {
            team.removeEntry(player.getName());
        }
    }

    public void startNameTagTask() {
        // User recalculate (Prefix/Suffix change)
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
                    // Update everyone because we don't know who was in that group easily
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        updateNameTag(player);
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
    }
}
