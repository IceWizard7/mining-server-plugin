package icewizard7.miningServerPlugin.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class JoinQuitManager {
    private final DiscordBridgeManager discordBridgeManager;
    private final StatManager statManager;

    public JoinQuitManager(DiscordBridgeManager discordBridgeManager, StatManager statManager) {
        this.discordBridgeManager = discordBridgeManager;
        this.statManager = statManager;
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

    public void joinEvent(Player player) {
        // Set the join message
        if (statManager.hasAlreadyJoined(player)) {
            sendJoinMessage(player);
        } else {
            sendWelcomeMessage(player);
        }
    }

    public void quitEvent(Player player, boolean isVanished) {
        // Set the quit message
        if (!isVanished) {
            sendQuitMessage(player);
        }
        statManager.quitEvent(player);
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
