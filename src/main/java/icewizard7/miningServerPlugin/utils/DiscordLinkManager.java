package icewizard7.miningServerPlugin.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class DiscordLinkManager {

    private final Plugin plugin;
    private final Map<String, UUID> pendingCodes = new HashMap<>();
    private final File file;
    private FileConfiguration data;

    public DiscordLinkManager(Plugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "linked-accounts.yml");

        if (!file.exists()) plugin.saveResource("linked-accounts.yml", false);
        data = YamlConfiguration.loadConfiguration(file);
    }

    public String createCode(UUID uuid) {
        String code = String.valueOf(100000 + new Random().nextInt(900000));
        pendingCodes.put(code, uuid);
        return code;
    }

    public UUID consumeCode(String code) {
        return pendingCodes.remove(code);
    }

    public void link(UUID uuid, String discordId) {
        data.set("links." + uuid.toString(), discordId);
        save();
    }

    public String getDiscordId(UUID uuid) {
        return data.getString("links." + uuid.toString());
    }

    private void save() {
        try { data.save(file); } catch (IOException e) { e.printStackTrace(); }
    }
}
