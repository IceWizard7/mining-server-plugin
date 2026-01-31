package icewizard7.miningServerPlugin.events;

import icewizard7.miningServerPlugin.utils.DiscordBridgeManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import io.papermc.paper.event.player.AsyncChatEvent;

public class ChatListener implements Listener {

    private final LuckPerms luckPerms;
    private final DiscordBridgeManager discordBridgeManager;
    private long discordLastSent = 0;

    public ChatListener(DiscordBridgeManager discordBridgeManager, LuckPerms luckPerms) {
        this.discordBridgeManager = discordBridgeManager;
        this.luckPerms = luckPerms;
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());

        sendDiscordMessage(player, event.message());

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

    public void sendDiscordMessage(Player player, Component message) {
        if (!discordBridgeManager.isReady()) return;

        String chatMessage = PlainTextComponentSerializer.plainText()
                .serialize(message);

        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) return;

        QueryOptions queryOptions = luckPerms.getContextManager().getQueryOptions(player);

        String prefix = cleanMeta(user.getCachedData().getMetaData(queryOptions).getPrefix());
        String suffix = cleanMeta(user.getCachedData().getMetaData(queryOptions).getSuffix());

        if (!prefix.isEmpty()) {
            prefix = "[" + prefix + "] ";
        }

        if (!suffix.isEmpty()) {
            suffix = " [" + suffix + "]";
        }

        String finalMessage = "**" + prefix + player.getName() + suffix + "**: " + chatMessage;

        if (System.currentTimeMillis() - discordLastSent > 500) {
            discordBridgeManager.getChatChannel()
                    .sendMessage(finalMessage)
                    .queue();
            discordLastSent = System.currentTimeMillis();
        }
    }

    private String cleanMeta(String input) {
        if (input == null) return "";

        // Remove hex colors like &#12ABEF
        input = input.replaceAll("&#[0-9a-fA-F]{6}", "");

        // Remove normal color/format codes (&a, &l, &r etc.)
        input = input.replaceAll("&[0-9a-fk-or]", "");

        // Remove section sign codes (§a etc.) just in case
        input = input.replaceAll("§[0-9a-fk-or]", "");

        return input.trim();
    }
}
