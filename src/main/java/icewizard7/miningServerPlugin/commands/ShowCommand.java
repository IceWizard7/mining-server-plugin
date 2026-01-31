package icewizard7.miningServerPlugin.commands;

import net.kyori.adventure.key.Key;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEvent.Action;
// import net.kyori.adventure.inventory.ItemStackSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ShowCommand implements TabExecutor {
    private final Plugin plugin;

    public ShowCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(Component.text("Usage: /show <slot>", NamedTextColor.RED));
            return true;
        }

        String field = args[0].toLowerCase();
        final PlayerInventory playerInventory = player.getInventory();

        ItemStack fieldItem = switch (field) {
            case "hand" -> playerInventory.getItemInMainHand();
            case "offhand" -> playerInventory.getItemInOffHand();
            case "helmet" -> playerInventory.getHelmet();
            case "chestplate" -> playerInventory.getChestplate();
            case "leggings" -> playerInventory.getLeggings();
            case "boots" -> playerInventory.getBoots();
            default -> null;
        };

        // Check if item is null or AIR (Material.AIR)
        if (fieldItem == null || fieldItem.getType().isAir()) {
            player.sendMessage(Component.text("Slot \"" + field + "\" is empty.", NamedTextColor.RED));
            return true;
        }

        // Get the display name
        // If it has a custom name, use it
        // Otherwise, use the localized vanilla name.
        Component displayComponent;
        if (fieldItem.hasItemMeta() && fieldItem.getItemMeta().hasDisplayName()) {
            displayComponent = fieldItem.getItemMeta().displayName();
        } else {
            // This ensures it says "Diamond Sword" correctly in the client's language
            displayComponent = Component.translatable(fieldItem.getType().translationKey(), NamedTextColor.WHITE);
        }

        // Add the Hover Event
        // 'asHoverEvent()' serializes the NBT (Enchants, Lore, etc.) automatically.
        Component itemComponent = displayComponent.hoverEvent(fieldItem.asHoverEvent());

        // Build the final message
        Component message = Component.text(player.getName() + "'s " + field + ": ", NamedTextColor.WHITE)
                .append(itemComponent);

        plugin.getServer().broadcast(message);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        if (args.length != 1) {
            return new ArrayList<>();
        }

        List<String> availableFields = new ArrayList<>();
        availableFields.add("hand");
        availableFields.add("offhand");
        availableFields.add("helmet");
        availableFields.add("chestplate");
        availableFields.add("leggings");
        availableFields.add("boots");

        String current = args[0].toLowerCase();

        return availableFields.stream()
                .filter(field -> field.toLowerCase().startsWith(current))
                .collect(Collectors.toList());
    }
}
