package icewizard7.miningServerPlugin.events;

import icewizard7.miningServerPlugin.commands.AutoCompressCommand;
import icewizard7.miningServerPlugin.utils.AutoCompressManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class TelepathyListener implements Listener {

    private final Plugin plugin;
    private final AutoCompressManager autoCompressManager;

    public TelepathyListener(Plugin plugin, AutoCompressManager autoCompressManager) {
        this.plugin = plugin;
        this.autoCompressManager = autoCompressManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location loc = block.getLocation();

        // Delay 1 tick to let custom drops spawn
        Bukkit.getScheduler().runTask(plugin, () -> {
            boolean inventoryFull = false;

            for (Entity entity : loc.getWorld().getNearbyEntities(loc, 1, 1, 1)) {
                if (!(entity instanceof Item itemEntity)) continue;

                ItemStack stack = itemEntity.getItemStack();
                var leftover = player.getInventory().addItem(stack);

                if (!leftover.isEmpty()) {
                    inventoryFull = true;
                    // Do not drop leftover
                }

                // Remove the entity so it never drops on the ground
                itemEntity.remove();
            }

            if (inventoryFull) {
                Component msg = Component.text("Your inventory is full!")
                        .color(NamedTextColor.RED);
                player.sendMessage(msg);
                player.sendActionBar(msg);

                // If player has autocompress permission
                if (player.hasPermission("miningServerPlugin.autocompress")) {
                    // Automatically autocompress
                    autoCompressManager.autoCompress(player);
                }
            }
        });
    }
}
