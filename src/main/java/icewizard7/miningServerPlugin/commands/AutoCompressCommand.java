package icewizard7.miningServerPlugin.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.bukkit.inventory.meta.SkullMeta;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.kyori.adventure.text.format.TextDecoration;
import java.util.UUID;


import java.util.HashMap;
import java.util.Map;

public class AutoCompressCommand implements CommandExecutor {

    private final NamespacedKey compressKey;
    private final NamespacedKey superCompressKey;

    // Texture -> Display name
    private final Map<String, String> textureMap = new HashMap<>();

    public AutoCompressCommand(Plugin plugin) {
        this.compressKey = new NamespacedKey(plugin, "compressed");
        this.superCompressKey = new NamespacedKey(plugin, "super_compressed");
        textureMap.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzVlYmQ0OTljNmY4YmJjYTUyNTNhMzdiZjliYzM1NTdhMjAxMzZkYjRmMmU3OTllMTFiZjJlYThmMDljMGRlOSJ9fX0=", "Oak Log");
        textureMap.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWYxMjgzMTFmOTBiOGJiYWJmY2Q3NGFmYzI3YWU4ZTBiNGVkY2I0YTJiNDkwODljOGRmMGU4MTg5OTcwM2ZmOSJ9fX0=", "Stone Block");
        textureMap.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWMyNjU4MDNkMWNjMmUzMWY3OTUxZmZlY2JlZjUwZTA3OGMzNjYyOWQ1ZDA5MDc4YjkxYmE0ZGNkNDRjYTI5YyJ9fX0=", "Copper Block");
        textureMap.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzYxODczMWUwNjMzNzlhZWJmODJmMWQ2NGM0MTljOTBkN2YwYzE2NDhjNTQ4ZTliNjE1MWIxYmFiYTY2ZDcyMyJ9fX0=", "Coal Block");
        textureMap.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzQzZTgzNTg4ZTk2NWM4MzU5ZTY0YmJjN2ZlNTNmNDgxY2NhNGY3ZmM2YzI3NzE0ZTRlYWNlOWY1ZTliMDhiZCJ9fX0=", "Iron Block");
        textureMap.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDFiNWZkZWZmZmZmYTcwODM4MWU3MGYzNTAzYTI3NTc3MmI0NTI5NmNmOWYxNjI1YTg3ZWRjNmI2MjU0OWVmNiJ9fX0=", "Gold Block");
        textureMap.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODg5ODg1MjNmMjYzMWRlNWNiMDFmZGVhMzg3MDViNjRlYjkwNjY2N2Q4ZDk5YmNiODU5YTBhMTZkYjU5MWE3OCJ9fX0=", "Redstone Block");
        textureMap.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmMwZTZkOWUyNDI3MzU0ODE5MThjNWZkMTQ0OThiZDc2MGJiOWY0ZmY2NDMwYWQ0Njk2YjM4ZThhODgzZGE5NyJ9fX0=", "Emerald Block");
        textureMap.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2I1ZmFmNGNkODcxMzhjODcxY2M2YTg2NzU4MTdhODk5ODVhM2NiODk3MjFhNGM3NjJmZTY2NmZmNjE4MWMyNCJ9fX0=", "Diamond Block");
        textureMap.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDNiNTAwMWE2NzAwN2E5M2FkZTY2NTkxNzVlOTA1NGE5Mjk3NmIwODdmYzYyOWYwNDJiY2Q1N2U3NzU3NjQ2YSJ9fX0=", "Quartz Block");
        textureMap.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDgyYmI1MThlMzg4NGQ2MDEyYmJkYmFiNjQ0ZGI3OTBhMjM4NDJkYTVjNmFmNjQ1ZjZlMjhmNmNlODMzNjc3NiJ9fX0=", "Blackstone");
        textureMap.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjk4MGZlNzYyZjQ4ODYxMzk3ZjBjOGRjMmY4ZDEzZjdjMTY2MTcwMDM4ZTk3MzAyMDY4OTE5MmE4OTMwOGYzZCJ9fX0=", "Netherite Block");
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(Component.text(
                    "Only players can execute this command.", NamedTextColor.RED
            ));
            return true;
        }

        Player player = (Player) commandSender;

        return autoCompress(player);
    }

    public boolean autoCompress(Player player) {
        if (!(player.hasPermission("miningServerPlugin.autocompress"))) {
            player.sendMessage(Component.text(
                    "You do not have permission to use this command.", NamedTextColor.RED
            ));
            return true;
        }

        boolean compressedAnything = false;

        // Loop through all textures to compress normally
        for (Map.Entry<String, String> entry : textureMap.entrySet()) {
            String texture = entry.getKey();
            String name = entry.getValue();

            int total = countCompressible(player.getInventory(), texture);
            if (total >= 64) {
                int compressedAmount = total / 64;
                removeCompressible(player.getInventory(), texture, compressedAmount * 64);

                ItemStack stack = createCompressedHead(texture, name);
                stack.setAmount(compressedAmount);
                addToInventoryOrDrop(player, stack);

                compressedAnything = true;
            }
        }

        // Now handle super compression
        for (Map.Entry<String, String> entry : textureMap.entrySet()) {
            String texture = entry.getKey();
            String name = entry.getValue();

            int total = countSuperCompressible(player.getInventory(), texture);
            if (total >= 64) {
                int superAmount = total / 64;
                removeSuperCompressible(player.getInventory(), texture, superAmount * 64);

                ItemStack superStack = createSuperCompressedHead(texture, name);
                superStack.setAmount(superAmount);
                addToInventoryOrDrop(player, superStack);

                compressedAnything = true;
            }
        }


        if (compressedAnything) {
            player.sendMessage(Component.text("Inventory compressed.", NamedTextColor.GREEN));
        } else {
            player.sendMessage(Component.text("Nothing to compress.", NamedTextColor.RED));
        }

        return true;
    }

    private int countCompressible(Inventory inv, String texture) {
        int total = 0;

        for (ItemStack item : inv.getContents()) {
            if (item == null || item.getType() != Material.PLAYER_HEAD) continue;

            if (isCompressed(item)) continue;
            if (isSuperCompressed(item)) continue;

            String itemTex = getTexture(item);
            if (texture.equals(itemTex)) {
                total += item.getAmount();
            }
        }

        return total;
    }

    private void removeCompressible(Inventory inv, String texture, int amount) {
        int left = amount;

        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item == null || item.getType() != Material.PLAYER_HEAD) continue;
            if (isCompressed(item)) continue;
            if (isSuperCompressed(item)) continue;

            String itemTex = getTexture(item);
            if (!texture.equals(itemTex)) continue;

            if (item.getAmount() <= left) {
                left -= item.getAmount();
                inv.setItem(i, null);
            } else {
                item.setAmount(item.getAmount() - left);
                break;
            }

            if (left <= 0) break;
        }
    }

    private ItemStack createCompressedHead(String texture, String name) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();

        meta.displayName(
                Component.text("[Compressed] ", NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false)
                        .append(
                                Component.text(name, NamedTextColor.GOLD)
                                        .decoration(TextDecoration.ITALIC, false)
                        )
        );

        meta.getPersistentDataContainer().set(compressKey,
                PersistentDataType.BYTE, (byte) 1);

        // Deterministic UUID from texture
        UUID uuid = UUID.nameUUIDFromBytes(texture.getBytes());
        PlayerProfile profile = Bukkit.createProfile(uuid);
        profile.setProperty(new ProfileProperty("textures", texture));
        meta.setPlayerProfile(profile);

        item.setItemMeta(meta);
        return item;
    }

    private String getTexture(ItemStack item) {
        if (!(item.getItemMeta() instanceof SkullMeta meta)) return null;

        PlayerProfile profile = meta.getPlayerProfile();
        if (profile == null) return null;

        for (ProfileProperty prop : profile.getProperties()) {
            if (prop.getName().equals("textures") && prop.getValue() != null) {
                return prop.getValue();
            }
        }
        return null;
    }

    private boolean isCompressed(ItemStack item) {
        return item.hasItemMeta() &&
                item.getItemMeta().getPersistentDataContainer().has(
                        compressKey,
                        PersistentDataType.BYTE
                );
    }

    private int countSuperCompressible(Inventory inv, String texture) {
        int total = 0;

        for (ItemStack item : inv.getContents()) {
            if (item == null || item.getType() != Material.PLAYER_HEAD) continue;
            if (!isCompressed(item) || isSuperCompressed(item)) continue;

            String itemTex = getTexture(item);
            if (texture.equals(itemTex)) {
                total += item.getAmount();
            }
        }

        return total;
    }

    private void removeSuperCompressible(Inventory inv, String texture, int amount) {
        int left = amount;

        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item == null || item.getType() != Material.PLAYER_HEAD) continue;
            if (!isCompressed(item) || isSuperCompressed(item)) continue;

            String itemTex = getTexture(item);
            if (!texture.equals(itemTex)) continue;

            if (item.getAmount() <= left) {
                left -= item.getAmount();
                inv.setItem(i, null);
            } else {
                item.setAmount(item.getAmount() - left);
                break;
            }

            if (left <= 0) break;
        }
    }

    private ItemStack createSuperCompressedHead(String texture, String name) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();

        meta.displayName(
                Component.text("[Super Compressed] ", NamedTextColor.DARK_RED)
                        .decoration(TextDecoration.ITALIC, false)
                        .append(
                                Component.text(name, NamedTextColor.GOLD)
                                        .decoration(TextDecoration.ITALIC, false)
                        )
        );

        meta.getPersistentDataContainer().set(superCompressKey, PersistentDataType.BYTE, (byte) 1);

        // Deterministic UUID from texture
        UUID uuid = UUID.nameUUIDFromBytes(("super_" + texture).getBytes());
        PlayerProfile profile = Bukkit.createProfile(uuid);
        profile.setProperty(new ProfileProperty("textures", texture));
        meta.setPlayerProfile(profile);

        item.setItemMeta(meta);
        return item;
    }

    private boolean isSuperCompressed(ItemStack item) {
        return item.hasItemMeta() &&
                item.getItemMeta().getPersistentDataContainer().has(superCompressKey, PersistentDataType.BYTE);
    }

    private void addToInventoryOrDrop(Player player, ItemStack stack) {
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(stack);
        for (ItemStack drop : leftover.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), drop);
        }
    }
}
