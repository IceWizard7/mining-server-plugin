package icewizard7.miningServerPlugin.events;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import io.papermc.paper.event.player.AsyncChatEvent;

public class ChatEvent implements Listener {

    private final LuckPerms luckPerms;

    public ChatEvent(LuckPerms luckPerms) {
        this.luckPerms = luckPerms;
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());

        if (user != null) {
            // Get the group object
            String groupName = user.getPrimaryGroup();
            Group group = luckPerms.getGroupManager().getGroup(groupName);

            if (group != null) {
                // Get the metadata using player context
                QueryOptions queryOptions = luckPerms.getContextManager().getQueryOptions(player);
                String prefix = user.getCachedData().getMetaData(queryOptions).getPrefix();
                String suffix = user.getCachedData().getMetaData(queryOptions).getSuffix();

                // Convert Strings to Components (handling the '&' colour codes)
                var serializer = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand();

                net.kyori.adventure.text.Component prefixComp = (prefix != null) ? serializer.deserialize(prefix) : net.kyori.adventure.text.Component.empty();
                net.kyori.adventure.text.Component suffixComp = (suffix != null) ? serializer.deserialize(suffix) : net.kyori.adventure.text.Component.empty();

                // Apply the Renderer (This replaces .setFormat)
                event.renderer((source, sourceDisplayName, message, viewer) -> {
                    return net.kyori.adventure.text.Component.text()
                            .append(prefixComp)
                            .append(sourceDisplayName) // Player's name
                            .append(suffixComp)
                            .append(net.kyori.adventure.text.Component.text(": "))
                            .append(message)           // Chat message
                            .build();
                });
            }
        }
    }
}
