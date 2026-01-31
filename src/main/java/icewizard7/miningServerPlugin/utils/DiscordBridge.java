package icewizard7.miningServerPlugin.utils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
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
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

public final class DiscordBridge {

    private JDA jda;
    private final DiscordLinkManager discordLinkManager;
    private volatile TextChannel chatChannel;
    private volatile TextChannel consoleChannel;
    private final Plugin plugin;
    private final Logger logger;
    private final File file;
    private FileConfiguration data;

    private final Queue<String> consoleQueue = new ConcurrentLinkedQueue<>();
    private Appender log4jAppender;
    private int consoleTaskId = -1;

    public DiscordBridge(Plugin plugin, DiscordLinkManager discordLinkManager) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.discordLinkManager = discordLinkManager;

        this.file = new File(plugin.getDataFolder(), "discord-credentials.yml");

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        data = YamlConfiguration.loadConfiguration(file);
    }

    public void enable() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String token = data.getString("discord.token");
                String chatId = data.getString("discord.chat_channel");
                String consoleId = data.getString("discord.console_channel");

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
                if (channelId.equals(data.getString("discord.chat_channel"))) {
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
                else if (channelId.equals(data.getString("discord.console_channel"))) {
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

                        UUID uuid = discordLinkManager.consumeCode(code);

                        if (uuid == null) {
                            event.getChannel().sendMessage("Invalid or expired code.").queue();
                            return;
                        }

                        discordLinkManager.link(uuid, event.getAuthor().getId());

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

    public void disable() {
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

    public String getAvatarUrl(Player player) {
        return "https://mc-heads.net/avatar/" + player.getUniqueId();
    }

    public void sendJoinEmbed(Player player) {
        String avatarUrl = getAvatarUrl(player);

        TextChannel channel = this.getChatChannel();

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

        EmbedBuilder embed = new EmbedBuilder();

        embed.setColor(Color.RED);
        embed.setTitle("[-] " + player.getName());
        embed.setThumbnail(avatarUrl);
        embed.setDescription("Player left the server.");

        embed.setFooter("Minecraft Server", null);

        channel.sendMessageEmbeds(embed.build()).queue();
    }

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

    public String getBotName() {
        return jda != null ? jda.getSelfUser().getName() : "DiscordBot";
    }

    public TextChannel getChatChannel() { return chatChannel; }
    public TextChannel getConsoleChannel() { return consoleChannel; }
}
