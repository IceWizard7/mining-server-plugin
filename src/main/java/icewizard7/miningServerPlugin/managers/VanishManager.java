package icewizard7.miningServerPlugin.managers;

import org.bukkit.Bukkit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VanishManager {
    private final Plugin plugin;
    private final Set<UUID> vanishedPlayers = new HashSet<>();;
    private final JoinQuitManager joinQuitManager;

    public VanishManager(Plugin plugin, JoinQuitManager joinQuitManager) {
        this.plugin = plugin;
        this.joinQuitManager = joinQuitManager;
    }

    private void fakeJoinMessage(Player player) {
        joinQuitManager.sendJoinMessage(player);
    }

    private void fakeQuitMessage(Player player) {
        joinQuitManager.sendQuitMessage(player);
    }

    public boolean isPlayerVanished(Player player) {
        return vanishedPlayers.contains(player.getUniqueId());
    }

    public void vanishCommandPlayer(Player player) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.hidePlayer(plugin, player);
        }
        fakeQuitMessage(player);
        addVanishedPlayer(player);
        player.sendMessage(Component.text(
                "Vanish mode enabled.", NamedTextColor.GREEN
        ));
    }

    public void unvanishCommandPlayer(Player player) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showPlayer(plugin, player);
        }
        fakeJoinMessage(player);
        removeVanishedPlayer(player);
        player.sendMessage(Component.text(
                "Vanish mode disabled.", NamedTextColor.RED
        ));
    }

    public void addVanishedPlayer(Player player) {
        vanishedPlayers.add(player.getUniqueId());
    }

    public void removeVanishedPlayer(Player player) {
        vanishedPlayers.remove(player.getUniqueId());
    }

    public Set<UUID> getVanishedPlayers() {
        return vanishedPlayers;
    }

    public int getAmountVanishedPlayers() {
        return vanishedPlayers.size();
    }

    public void joinEvent(Player player) {
        for (UUID vanishedPlayerUUID : getVanishedPlayers()) {
            Player vanishedPlayer = Bukkit.getPlayer(vanishedPlayerUUID);
            if (vanishedPlayer != null) {
                player.hidePlayer(plugin, vanishedPlayer);
            }
        }
    }

    public void quitEvent(Player player) {
        removeVanishedPlayer(player);
    }
}
