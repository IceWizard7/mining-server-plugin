package icewizard7.miningServerPlugin.events;

import icewizard7.miningServerPlugin.utils.DiscordBridge;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DiscordChatEvent implements Listener {
    private final DiscordBridge discordBridge;
    private final LuckPerms luckPerms;
    private long lastSent = 0;
    private final LegacyComponentSerializer serializer =
            LegacyComponentSerializer.legacyAmpersand();

    public DiscordChatEvent(DiscordBridge discordBridge, LuckPerms luckPerms) {
        this.discordBridge = discordBridge;
        this.luckPerms = luckPerms;
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        if (!discordBridge.isReady()) return;

        Player player = event.getPlayer();
        String chatMessage = PlainTextComponentSerializer.plainText()
                .serialize(event.message());

        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) return;

        QueryOptions queryOptions = luckPerms.getContextManager().getQueryOptions(player);

        String prefix = cleanMeta(user.getCachedData().getMetaData(queryOptions).getPrefix());
        String suffix = cleanMeta(user.getCachedData().getMetaData(queryOptions).getSuffix());

        String finalMessage = "**[" + prefix + "] " + player.getName() + " [" + suffix + "]**: " + chatMessage;

        if (System.currentTimeMillis() - lastSent > 500) {
            discordBridge.getChatChannel()
                    .sendMessage(finalMessage)
                    .queue();
            lastSent = System.currentTimeMillis();
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
