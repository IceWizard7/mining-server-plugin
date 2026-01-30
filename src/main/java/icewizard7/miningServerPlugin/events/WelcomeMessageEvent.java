package icewizard7.miningServerPlugin.events;

import icewizard7.miningServerPlugin.utils.DiscordBridge;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class WelcomeMessageEvent implements Listener {

    private final DiscordBridge discordBridge;

    public WelcomeMessageEvent(DiscordBridge discordBridge) {
        this.discordBridge = discordBridge;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String playerName = event.getPlayer().getName();

        // Build the join message
        Component joinMessage = Component.text("[", NamedTextColor.WHITE)
                .append(Component.text("+", NamedTextColor.GREEN))
                .append(Component.text("] " + playerName, NamedTextColor.WHITE));

        // Set the join message
        event.joinMessage(joinMessage);
        // discordBridge.getChatChannel().sendMessage("[+] " + playerName).queue();
        discordBridge.sendJoinEmbed(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        String playerName = event.getPlayer().getName();

        // Build the join message
        Component quitMessage = Component.text("[", NamedTextColor.WHITE)
                .append(Component.text("-", NamedTextColor.RED))
                .append(Component.text("] " + playerName, NamedTextColor.WHITE));

        // Set the join message
        event.quitMessage(quitMessage);
        // discordBridge.getChatChannel().sendMessage("[-] " + playerName).queue();
        discordBridge.sendQuitEmbed(event.getPlayer());
    }
}
