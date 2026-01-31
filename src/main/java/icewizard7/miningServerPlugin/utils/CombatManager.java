package icewizard7.miningServerPlugin.utils;

import org.bukkit.Bukkit;
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
    private static final long COMBAT_TIME = 10_000;
    private BukkitTask combatTask;

    public CombatManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public void tagPlayer(Player player) {
        long expireTime = System.currentTimeMillis() + COMBAT_TIME;
        combatTag.put(player.getUniqueId(), expireTime);
    }

    public void untagPlayer(Player player) {
        combatTag.remove(player.getUniqueId());
    }

    public boolean isInCombat(Player player) {
        Long expire = combatTag.get(player.getUniqueId());
        if (expire == null) return false;

        if (System.currentTimeMillis() > expire) {
            combatTag.remove(player.getUniqueId()); // cleanup
            return false;
        }

        return true;
    }

    private long getRemainingCombatTime(Player player) {
        Long expire = combatTag.get(player.getUniqueId());
        if (expire == null) return 0;
        return Math.max(0, (expire - System.currentTimeMillis()) / 1000);
    }

    public void sendCombatBar(Player player) {
        Component msg = Component.text("You are in combat: " + getRemainingCombatTime(player) + "s", NamedTextColor.RED);
        player.sendActionBar(msg);
    }

    public void sendCombatMessage(Player player) {
        Component msg = Component.text("You are in combat: " + getRemainingCombatTime(player) + "s", NamedTextColor.RED);
        player.sendMessage(msg);
    }

    public void startCombatTask() {
        this.combatTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {

            for (Player player : Bukkit.getOnlinePlayers()) {

                if (!isInCombat(player)) continue;

                sendCombatBar(player);
            }

        }, 0L, 20L);
    }

    public void shutdown() {
        if (combatTask != null && !combatTask.isCancelled()) {
            combatTask.cancel();
        }
    }
}
