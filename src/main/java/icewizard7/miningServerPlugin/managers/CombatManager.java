package icewizard7.miningServerPlugin.managers;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatManager {
    private final Plugin plugin;
    private final WorldGuardManager worldGuardManager;
    private final Map<UUID, Long> combatTag = new HashMap<>();
    private static final long COMBAT_TIME = 15_000; // 15 seconds
    private BukkitTask combatTask;


    public CombatManager(Plugin plugin, WorldGuardManager worldGuardManager) {
        this.plugin = plugin;
        this.worldGuardManager = worldGuardManager;
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

    public void quitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        untagPlayer(player);
    }

    public void combatEvent(EntityDamageByEntityEvent event) {
        Entity attacker = event.getDamager();
        Entity victim = event.getEntity();

        // If you are in a region (ex. Spawn), where PvP isn't even allowed; Just don't tag anything.
        if (!worldGuardManager.inPvPAllowedRegion(victim)) return;

        // If victim is Player -> tag
        if (victim instanceof Player player) {
            tagPlayer(player);
        }

        // Melee
        if (attacker instanceof Player player) {
            tagPlayer(player);
        }

        // Projectile
        if (attacker instanceof Projectile projectile) {
            if (projectile.getShooter() instanceof Player player) {
                tagPlayer(player);
            }
        }
    }

    public void elytraToggleEvent(EntityToggleGlideEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (!event.isGliding()) {
            return;
        }

        // Player is starting to glide

        // Check if player is in combat
        if (isInCombat(player) && !worldGuardManager.isInRegion(player, "pvp")) {
            player.sendMessage(Component.text("You cannot use Elytra while in combat!", NamedTextColor.RED));
            event.setCancelled(true); // Blocks Elytra flight
        }
    }

    public void joinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            AttributeInstance attribute = player.getAttribute(Attribute.MAX_HEALTH);
            if (attribute != null) {
                player.setHealth(attribute.getValue());
            }
        }, 2L);  // delay by 2 ticks (just to be safe)
    }

    public void deathEvent(PlayerDeathEvent event) {
        Player player = event.getPlayer();

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
