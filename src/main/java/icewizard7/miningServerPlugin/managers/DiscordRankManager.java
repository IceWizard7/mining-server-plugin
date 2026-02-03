package icewizard7.miningServerPlugin.managers;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.EventSubscription;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public class DiscordRankManager {
    private final Plugin plugin;
    private final LuckPerms luckPerms;
    private final LuckPermsManager luckPermsManager;
    private final DiscordBridgeManager discordBridgeManager;

    private EventSubscription<UserDataRecalculateEvent> userRecalculateTask;
    private EventSubscription<net.luckperms.api.event.group.GroupDataRecalculateEvent> groupRecalculateTask;

    public DiscordRankManager(Plugin plugin, LuckPerms luckPerms, LuckPermsManager luckPermsManager, DiscordBridgeManager discordBridgeManager) {
        this.plugin = plugin;
        this.luckPerms = luckPerms;
        this.luckPermsManager = luckPermsManager;
        this.discordBridgeManager = discordBridgeManager;
    }

    private void updateRank(Player player) {
        UUID uuid = player.getUniqueId();

        // Account not linked
        if (!discordBridgeManager.isLinked(uuid)) {
            return;
        }

        User user = luckPermsManager.getOrLoadUser(player.getUniqueId());

        String discordID = discordBridgeManager.getDiscordId(uuid);
        String groupName = user.getPrimaryGroup();
        String discordRoleId = discordBridgeManager.getRoleIdByGroupName(groupName);

        if (discordRoleId == null) return;

        // Remove all ranked roles
        discordBridgeManager.removeRankedRoles(discordID);

        // Give correct role
        discordBridgeManager.giveRole(discordID, discordRoleId);
    }

    public void startRankTask() {
        // User recalculate (Prefix/Suffix change)
        this.userRecalculateTask = luckPerms.getEventBus().subscribe(plugin,
                net.luckperms.api.event.user.UserDataRecalculateEvent.class,
                event -> Bukkit.getScheduler().runTask(plugin, () -> {
                    Player player = Bukkit.getPlayer(event.getUser().getUniqueId());
                    if (player != null) {
                        updateRank(player);
                    }
                })
        );

        // Group recalculate
        this.groupRecalculateTask = luckPerms.getEventBus().subscribe(plugin,
                net.luckperms.api.event.group.GroupDataRecalculateEvent.class,
                event -> Bukkit.getScheduler().runTask(plugin, () -> {
                    // Update everyone because we don't know who was in that group easily
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player != null) {
                            updateRank(player);
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
    }
}
