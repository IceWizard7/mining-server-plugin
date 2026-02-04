package icewizard7.miningServerPlugin.managers;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.LoggerContext;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

public final class DiscordBridgeManager {

    private JDA jda;

    private volatile TextChannel chatChannel;
    private volatile TextChannel consoleChannel;
    private volatile String guildID;
    private volatile String linkedRole;
    private final Map<String, String> discordRankRoles = new HashMap<>();

    private final Plugin plugin;
    private final Logger logger;
    private final File discordCredentialsFile;
    private final File linkedAccountsFile;
    private FileConfiguration discordCredentialsData;
    private FileConfiguration linkedAccountsData;

    private final Queue<String> consoleQueue = new ConcurrentLinkedQueue<>();
    private Appender log4jAppender;
    private int consoleTaskId = -1;

    private final Map<String, UUID> pendingLinkCodes = new HashMap<>();

    public DiscordBridgeManager(Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();

        this.discordCredentialsFile = new File(plugin.getDataFolder(), "discord-credentials.yml");

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        if (!discordCredentialsFile.exists()) {
            try {
                discordCredentialsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        discordCredentialsData = YamlConfiguration.loadConfiguration(discordCredentialsFile);

        this.linkedAccountsFile = new File(plugin.getDataFolder(), "linked-accounts.yml");

        if (!linkedAccountsFile.exists()) {
            try {
                linkedAccountsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        linkedAccountsData = YamlConfiguration.loadConfiguration(linkedAccountsFile);
    }

    private void startLogForwarding() {
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();

        // Define the Appender
        log4jAppender = new AbstractAppender("DiscordConsoleAppender", null, null, false, null) {
            @Override
            public void append(LogEvent event) {
                String message = event.getMessage().getFormattedMessage();

                // Prevent infinite loops and ignore empty lines
                if (message.contains("Discord bridge") || message.trim().isEmpty()) return;

                String level = event.getLevel().toString();
                consoleQueue.add("[" + level + "] " + message);
            }
        };

        // Start and Register Appender
        log4jAppender.start();
        config.addAppender(log4jAppender);
        config.getRootLogger().addAppender(log4jAppender, null, null);
        ctx.updateLoggers(); // Refresh loggers to apply changes

        // Batch sender task (runs every 2 seconds)
        consoleTaskId = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if (consoleChannel == null || consoleQueue.isEmpty()) return;

            StringBuilder sb = new StringBuilder();
            while (!consoleQueue.isEmpty() && sb.length() < 1800) {
                sb.append(consoleQueue.poll()).append("\n");
            }

            if (sb.length() > 0) {
                consoleChannel.sendMessage("```" + sb.toString() + "```").queue();
            }
        }, 40L, 40L).getTaskId();
    }

    private void startDiscordToGameListener() {
        jda.addEventListener(new ListenerAdapter() {
            @Override
            public void onMessageReceived(@NotNull MessageReceivedEvent event) {
                // Ignore bots (including ourselves)
                if (event.getAuthor().isBot()) return;

                String author = event.getAuthor().getGlobalName();
                String message = event.getMessage().getContentRaw();
                String channelId = event.getChannel().getId();

                // Handle Chat Channel -> In-game Chat
                if (channelId.equals(discordCredentialsData.getString("discord.chat_channel"))) {
                    Component gameMessage = Component.text("[Discord] ", NamedTextColor.BLUE)
                            .append(Component.text(author + ": ", NamedTextColor.GRAY))
                            .append(Component.text(message, NamedTextColor.WHITE));

                    // Broadcast to all players on the main thread
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        Bukkit.broadcast(gameMessage);
                    });
                    return;
                }

                // Handle Console Channel -> Execute Command
                else if (channelId.equals(discordCredentialsData.getString("discord.console_channel"))) {
                    // Execute command on the main thread
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        logger.info("[Discord Console] Executing: " + message);
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), message);
                    });
                    return;
                }

                // Link System
                if (event.isFromType(ChannelType.PRIVATE)) {
                    if (message.startsWith("!link ")) {
                        String code = message.substring(6).trim();

                        UUID uuid = consumeCode(code);

                        if (uuid == null) {
                            event.getChannel().sendMessage("Invalid or expired code.").queue();
                            return;
                        }

                        link(uuid, event.getAuthor().getId());

                        Player player = Bukkit.getPlayer(uuid);
                        if (player != null) {
                            Bukkit.getScheduler().runTask(plugin, () ->
                                    player.sendMessage(Component.text("Your account has been successfully linked to " + player.getName() + ".", NamedTextColor.GREEN))
                            );
                        }

                        event.getChannel().sendMessage("Your account has been successfully linked to " + author + ".").queue();
                    }
                }
            }
        });
    }

    public boolean isReady() {
        return chatChannel != null && consoleChannel != null;
    }

    private String getAvatarUrl(Player player) {
        return "https://mc-heads.net/avatar/" + player.getUniqueId();
    }

    // Join + Quit messages
    public void sendWelcomeEmbed(Player player) {
        String avatarUrl = getAvatarUrl(player);

        TextChannel channel = this.getChatChannel();

        if (channel == null) {
            return;
        }

        EmbedBuilder embed = new EmbedBuilder();

        embed.setColor(Color.PINK);
        embed.setTitle("[+] " + player.getName());
        embed.setThumbnail(avatarUrl);
        embed.setDescription("New player joined the server.");

        embed.setFooter("Minecraft Server", null);

        channel.sendMessageEmbeds(embed.build()).queue();
    }

    public void sendJoinEmbed(Player player) {
        String avatarUrl = getAvatarUrl(player);

        TextChannel channel = this.getChatChannel();

        if (channel == null) {
            return;
        }

        EmbedBuilder embed = new EmbedBuilder();

        embed.setColor(Color.GREEN);
        embed.setTitle("[+] " + player.getName());
        embed.setThumbnail(avatarUrl);
        embed.setDescription("Player joined the server.");

        embed.setFooter("Minecraft Server", null);

        channel.sendMessageEmbeds(embed.build()).queue();
    }

    public void sendQuitEmbed(Player player) {
        String avatarUrl = getAvatarUrl(player);

        TextChannel channel = this.getChatChannel();

        if (channel == null) {
            return;
        }

        EmbedBuilder embed = new EmbedBuilder();

        embed.setColor(Color.RED);
        embed.setTitle("[-] " + player.getName());
        embed.setThumbnail(avatarUrl);
        embed.setDescription("Player left the server.");

        embed.setFooter("Minecraft Server", null);

        channel.sendMessageEmbeds(embed.build()).queue();
    }

    public String getBotName() {
        return jda != null ? jda.getSelfUser().getName() : "DiscordBot";
    }

    public TextChannel getChatChannel() { return chatChannel; }
    public TextChannel getConsoleChannel() { return consoleChannel; }

    // Account linking
    public void sendLinkedInfo(Player player, String discordId) {
        if (jda == null) {
            player.sendMessage(Component.text("Discord connection not ready.", NamedTextColor.RED));
            return;
        }

        jda.retrieveUserById(discordId).queue(user -> {
            String name = user.getName();          // username
            String tag = user.getGlobalName();     // display name (if set)

            Bukkit.getScheduler().runTask(plugin, () -> {
                player.sendMessage(Component.text("Your Minecraft account is already linked.", NamedTextColor.RED));
                player.sendMessage(Component.text("Linked Discord account: ", NamedTextColor.GRAY)
                        .append(Component.text(tag != null ? tag : name, NamedTextColor.AQUA)));
                player.sendMessage(Component.text("Use /unlink to remove the connection.", NamedTextColor.YELLOW));
            });

        }, error -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                player.sendMessage(Component.text("Your account is linked, but Discord user could not be fetched.", NamedTextColor.RED));
                player.sendMessage(Component.text("Use /unlink to reset the link.", NamedTextColor.YELLOW));
            });
        });
    }

    public String createCode(UUID uuid) {
        String code = String.valueOf(100000 + new Random().nextInt(900000));
        pendingLinkCodes.put(code, uuid);
        return code;
    }

    public UUID consumeCode(String code) {
        return pendingLinkCodes.remove(code);
    }

    public void link(UUID uuid, String discordId) {
        linkedAccountsData.set("links." + uuid.toString(), discordId);
        giveRole(discordId, linkedRole);
        save();
    }

    private void save() {
        try { linkedAccountsData.save(linkedAccountsFile); } catch (IOException e) { e.printStackTrace(); }
    }

    public boolean isLinked(UUID uuid) {
        return linkedAccountsData.contains("links." + uuid.toString());
    }

    public void unlink(UUID uuid) {
        linkedAccountsData.set("links." + uuid.toString(), null);
        save();
    }

    public String getDiscordId(UUID uuid) {
        return linkedAccountsData.getString("links." + uuid.toString());
    }

    public List<String> getRoleIds(String discordId) {
        Guild guild = jda.getGuildById(guildID);
        if (guild == null) return Collections.emptyList();

        Member member = guild.retrieveMemberById(discordId).complete();
        List<Role> roles = member.getRoles();
        return roles.stream()
                .map(Role::getId)
                .toList();
    }

    public String getRoleIdByGroupName(String groupName) {
        return discordRankRoles.getOrDefault(groupName, null);
    }

    public boolean hasRole(String discordId, String roleId) {
        return getRoleIds(discordId).contains(roleId);
    }

    public void giveRole(String discordId, String roleId) {
        Guild guild = jda.getGuildById(guildID);
        if (guild == null) return;

        Member member = guild.retrieveMemberById(discordId).complete();
        if (member == null) return;

        if (roleId == null) return;

        Role role = guild.getRoleById(roleId);
        if (role == null) return;

        guild.addRoleToMember(member, role).queue();
    }

    public void removeRankedRoles(String discordId) {
        Collection<String> roleIds = discordRankRoles.values();

        for (String roleId : roleIds) {
            if (hasRole(discordId, roleId)) {
                removeRole(discordId, roleId);
            }
        }
    }

    public void removeRole(String discordId, String roleId) {
        Guild guild = jda.getGuildById(guildID);
        if (guild == null) return;

        Member member = guild.retrieveMemberById(discordId).complete();
        Role role = guild.getRoleById(roleId);
        if (role == null) return;
        guild.removeRoleFromMember(member, role).queue();
    }

    public void connect() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String token = discordCredentialsData.getString("discord.token");
                String chatId = discordCredentialsData.getString("discord.chat_channel");
                String consoleId = discordCredentialsData.getString("discord.console_channel");
                guildID = discordCredentialsData.getString("discord.guild_id");
                Map<String, Object> rawDiscordRankRoles = discordCredentialsData.getConfigurationSection("discord_rank_roles").getValues(false);
                linkedRole = discordCredentialsData.getString("discord_linked_role");

                for (Map.Entry<String, Object> e : rawDiscordRankRoles.entrySet()) {
                    Object value = e.getValue();
                    if (value instanceof String s) {
                        discordRankRoles.put(e.getKey(), s);
                    }
                }

                if (token == null || token.isEmpty()) {
                    logger.severe("Discord token missing in discord-credentials.yml.");
                    return;
                }

                // Inside the async task in enable()
                jda = JDABuilder.createDefault(token)
                        .enableIntents(
                                GatewayIntent.MESSAGE_CONTENT,
                                GatewayIntent.DIRECT_MESSAGES
                        )
                        .build()
                        .awaitReady();

                // After JDA is ready, start the listener
                startDiscordToGameListener();
                chatChannel = jda.getTextChannelById(chatId);
                consoleChannel = jda.getTextChannelById(consoleId);

                if (chatChannel == null || consoleChannel == null) {
                    logger.severe("Invalid Discord channel IDs.");
                    return;
                }

                logger.info("Discord bridge connection established.");

                getChatChannel().sendMessage("🟢 Minecraft Server started.").queue();

                // Start intercepting logs
                startLogForwarding();
            } catch (Exception e) {
                logger.severe("Discord bridge failed to connect.");
                e.printStackTrace();
            }
        });
    }

    public void shutdown() {
        if (log4jAppender != null) {
            final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
            ctx.getConfiguration().getRootLogger().removeAppender(log4jAppender.getName());
            log4jAppender.stop();
        }

        if (consoleTaskId != -1) {
            Bukkit.getScheduler().cancelTask(consoleTaskId);
        }

        if (chatChannel != null) {
            getChatChannel().sendMessage("🔴 Minecraft Server stopped.").queue();
        }

        if (jda != null) jda.shutdown();
    }
}
