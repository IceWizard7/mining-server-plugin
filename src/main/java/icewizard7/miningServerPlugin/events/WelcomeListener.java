package icewizard7.miningServerPlugin.events;

import icewizard7.miningServerPlugin.managers.DiscordBridgeManager;
import icewizard7.miningServerPlugin.managers.StatManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class WelcomeListener implements Listener {

    private final DiscordBridgeManager discordBridgeManager;
    private final StatManager statManager;

    public WelcomeListener(DiscordBridgeManager discordBridgeManager, StatManager statManager) {
        this.discordBridgeManager = discordBridgeManager;
        this.statManager = statManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();

        // Build the join message
        Component joinMessage = Component.text("[", NamedTextColor.WHITE)
                .append(Component.text("+", NamedTextColor.GREEN))
                .append(Component.text("] " + playerName, NamedTextColor.WHITE));

        // Set the join message
        if (!statManager.hasAlreadyJoined(player)) {
            Component welcomeMessage = Component.text("Welcome to FutureMines, " + playerName + "!", NamedTextColor.LIGHT_PURPLE);
            event.joinMessage(joinMessage.append(Component.newline()).append(welcomeMessage));
            discordBridgeManager.sendWelcomeEmbed(event.getPlayer());
        } else {
            event.joinMessage(joinMessage);
            discordBridgeManager.sendJoinEmbed(event.getPlayer());
        }
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
        discordBridgeManager.sendQuitEmbed(event.getPlayer());
    }
}
