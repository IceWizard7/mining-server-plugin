package icewizard7.miningServerPlugin.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Set;

public class TAB {
    private final Set<Player> vanishedPlayers;

    public TAB(Set<Player> vanishedPlayers) {
        this.vanishedPlayers = vanishedPlayers;
    }

    public void updateTab(Player player) {
        LuckPerms luckPerms = LuckPermsProvider.get();
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        Component playerListName = Component.text(player.getName()); // Default

        if (user != null) {
            QueryOptions queryOptions = luckPerms.getContextManager().getQueryOptions(player);
            String prefix = user.getCachedData().getMetaData(queryOptions).getPrefix();
            String suffix = user.getCachedData().getMetaData(queryOptions).getSuffix();

            var serializer = LegacyComponentSerializer.legacyAmpersand();

            Component prefixComp = (prefix != null) ? serializer.deserialize(prefix) : Component.empty();
            Component suffixComp = (suffix != null) ? serializer.deserialize(suffix) : Component.empty();

            // Combine Prefix + Name + Suffix
            playerListName = prefixComp.append(player.name()).append(suffixComp);
        }

        player.playerListName(playerListName);

        // Header
        Component header = Component.text(
                "Welcome to FutureMines", NamedTextColor.GOLD
        ).append(Component.newline());

        // RAM calculation
        Runtime runtime = Runtime.getRuntime();

        long maxMemory = runtime.maxMemory() / 1024 / 1024;     // MB
        long totalMemory = runtime.totalMemory() / 1024 / 1024; // MB
        long freeMemory = runtime.freeMemory() / 1024 / 1024;   // MB
        long usedMemory = totalMemory - freeMemory;

        double usagePercent = (usedMemory / (double) maxMemory) * 100;

        NamedTextColor ramColor;

        if (usagePercent < 50) {
            ramColor = NamedTextColor.GREEN;
        } else if (usagePercent < 75) {
            ramColor = NamedTextColor.GOLD;   // closest to orange in MC
        } else {
            ramColor = NamedTextColor.RED;
        }

        // TPS
        double tps = Bukkit.getServer().getTPS()[0];
        if (tps > 20.0) {
            tps = 20.0; // TPS cannot actually exceed 20 in practice
        }

        NamedTextColor tpsColor;
        if (tps > 18) {
            tpsColor = NamedTextColor.GREEN;
        } else if (tps > 15) {
            tpsColor = NamedTextColor.GOLD;
        } else {
            tpsColor = NamedTextColor.RED;
        }

        // Text
        Component playersOnlineLine = Component.newline().append(Component.text("Players online: " + (Bukkit.getOnlinePlayers().size() - vanishedPlayers.size()), NamedTextColor.GRAY)).append(Component.newline());
        Component ipLine = Component.text("futuremines.minekeep.gg", NamedTextColor.GRAY).append(Component.newline());
        Component ramLine = Component.text("RAM: " + usedMemory + "/" + maxMemory + " MB (", NamedTextColor.GRAY).append(Component.text((int) usagePercent + "%", ramColor)).append(Component.text(")", NamedTextColor.GRAY)).append(Component.newline());
        Component tpsLine = Component.text("TPS: ", NamedTextColor.GRAY).append(Component.text(String.format("%.2f", tps), tpsColor));

        Component footer = playersOnlineLine.append(ipLine).append(ramLine).append(tpsLine);

        // Send to player
        player.sendPlayerListHeaderAndFooter(header, footer);
    }
}
