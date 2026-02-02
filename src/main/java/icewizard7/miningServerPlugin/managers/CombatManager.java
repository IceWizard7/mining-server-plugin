package icewizard7.miningServerPlugin.managers;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatManager {
    private final Map<UUID, Long> combatTag = new HashMap<>();
    private final Plugin plugin;
    private static final long COMBAT_TIME = 15_000; // 15 seconds
    private BukkitTask combatTask;

    public CombatManager(Plugin plugin) {
        this.plugin = plugin;
    }

    // Tag player
    public void tagPlayer(Player player) {
        long expireTime = System.currentTimeMillis() + COMBAT_TIME;

        if (player.getAllowFlight()) {
            player.setAllowFlight(false);
        }

        combatTag.put(player.getUniqueId(), expireTime);
    }

    // Untag player and notify
    public void untagPlayer(Player player) {
        combatTag.remove(player.getUniqueId());
        Component msg = Component.text("You are out of combat.", NamedTextColor.GREEN);
        player.sendActionBar(msg);
        player.playSound(player.getLocation(), Sound.BLOCK_PISTON_EXTEND, 1f, 1f); // piston sound
    }

    // Check if player is in combat
    public boolean isInCombat(Player player) {
        Long expire = combatTag.get(player.getUniqueId());
        if (expire == null) return false;

        if (System.currentTimeMillis() > expire) {
            untagPlayer(player);
            return false;
        }

        return true;
    }

    // Get remaining combat time with 1 decimal
    public double getRemainingCombatTime(Player player) {
        Long expire = combatTag.get(player.getUniqueId());
        if (expire == null) return 0;
        double remaining = (expire - System.currentTimeMillis()) / 1000.0;
        return Math.max(0, Math.round(remaining * 10.0) / 10.0); // 1 decimal
    }

    // Action bar display
    public void sendCombatBar(Player player) {
        double remaining = getRemainingCombatTime(player);
        Component msg = Component.text("You are in combat: " + remaining + "s", NamedTextColor.RED);
        player.sendActionBar(msg);
    }

    // Send message in chat (the block message when /fly, etc. is entered)
    public void sendCombatMessage(Player player) {
        double remaining = getRemainingCombatTime(player);
        player.sendMessage(Component.text("You cannot do that while in combat. (" + remaining + "s left)", NamedTextColor.RED));
    }

    public void quitEvent(Player player) {
        untagPlayer(player);
    }

    // Repeating task
    public void startCombatTask() {
        combatTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {

            for (UUID uuid : combatTag.keySet().toArray(new UUID[0])) { // avoid concurrent mod
                Player player = Bukkit.getPlayer(uuid);
                if (player == null) continue;

                if (isInCombat(player)) {
                    sendCombatBar(player);
                }
            }

        }, 0L, 2L); // every 2 ticks = 0.1s
    }

    public void shutdown() {
        if (combatTask != null && !combatTask.isCancelled()) {
            combatTask.cancel();
        }
        combatTag.clear();
    }
}
