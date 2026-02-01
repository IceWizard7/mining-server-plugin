package icewizard7.miningServerPlugin.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;

import java.util.UUID;

public class LuckPermsManager {
    private final LuckPerms luckPerms;

    public LuckPermsManager(LuckPerms luckPerms) {
        this.luckPerms = luckPerms;
    }

    public User getOrLoadUser(UUID uuid) {
        User user = luckPerms.getUserManager().getUser(uuid);

        if (user == null) {
            user = luckPerms.getUserManager().loadUser(uuid).join(); // force sync
        }

        return user;
    }

    public String getStringPrefix(UUID uuid) {
        User user = getOrLoadUser(uuid);
        QueryOptions queryOptions = user.getQueryOptions();
        String prefix = user.getCachedData().getMetaData(queryOptions).getPrefix();
        return prefix != null ? prefix : "";
    }

    public String getStringSuffix(UUID uuid) {
        User user = getOrLoadUser(uuid);
        QueryOptions queryOptions = user.getQueryOptions();
        String suffix = user.getCachedData().getMetaData(queryOptions).getSuffix();
        return suffix != null ? suffix : "";
    }

    public Component getComponentPrefix(UUID uuid) {
        var serializer = LegacyComponentSerializer.legacyAmpersand();
        return serializer.deserialize(getStringPrefix(uuid));
    }

    public Component getComponentSuffix(UUID uuid) {
        var serializer = LegacyComponentSerializer.legacyAmpersand();
        return serializer.deserialize(getStringSuffix(uuid));
    }

    public int getWeight(UUID uuid) {
        User user = getOrLoadUser(uuid);

        if (user.getPrimaryGroup() == null) return 0;

        var group = luckPerms.getGroupManager().getGroup(user.getPrimaryGroup());
        return group != null ? group.getWeight().orElse(0) : 0;
    }
}
