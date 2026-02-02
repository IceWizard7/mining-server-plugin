package icewizard7.miningServerPlugin.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinQuitMessageManager {
    private final DiscordBridgeManager discordBridgeManager;
    private final StatManager statManager;
    private final VanishManager vanishManager;

    public JoinQuitMessageManager(DiscordBridgeManager discordBridgeManager, StatManager statManager, VanishManager vanishManager) {
        this.discordBridgeManager = discordBridgeManager;
        this.statManager = statManager;
        this.vanishManager = vanishManager;
    }

    public void sendJoinMessage(Player player) {
        Component joinMessage = getJoinMessage(player);
        Bukkit.broadcast(joinMessage);
        discordBridgeManager.sendJoinEmbed(player);
    }

    public void sendWelcomeMessage(Player player) {
        Component welcomeMessage = getWelcomeMessage(player);
        Bukkit.broadcast(welcomeMessage);
        discordBridgeManager.sendWelcomeEmbed(player);
    }

    public void sendQuitMessage(Player player) {
        Component quitMessage = getQuitMessage(player);
        Bukkit.broadcast(quitMessage);
        discordBridgeManager.sendQuitEmbed(player);
    }

    public void joinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (statManager.hasAlreadyJoined(player)) {
            sendJoinMessage(player);
        } else {
            sendWelcomeMessage(player);
        }

        event.joinMessage(null);
    }

    public void quitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        sendQuitMessage(player);

        event.quitMessage(null);
    }

    private Component getJoinMessage(Player player) {
        return Component.text("[", NamedTextColor.WHITE)
                .append(Component.text("+", NamedTextColor.GREEN))
                .append(Component.text("] " + player.getName(), NamedTextColor.WHITE));
    }

    private Component getWelcomeMessage(Player player) {
        return getJoinMessage(player)
                .append(Component.newline())
                .append(Component.text("Welcome to FutureMines, " + player.getName() + "!", NamedTextColor.LIGHT_PURPLE));
    }

    private Component getQuitMessage(Player player) {
        return Component.text("[", NamedTextColor.WHITE)
                .append(Component.text("-", NamedTextColor.RED))
                .append(Component.text("] " + player.getName(), NamedTextColor.WHITE));
    }
}
