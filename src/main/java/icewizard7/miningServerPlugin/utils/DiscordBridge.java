package icewizard7.miningServerPlugin.utils;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.LoggerContext;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

public final class DiscordBridge {

    private JDA jda;
    private volatile TextChannel chatChannel;
    private volatile TextChannel consoleChannel;
    private final Plugin plugin;
    private final Logger logger;
    private final FileConfiguration config;

    private final Queue<String> consoleQueue = new ConcurrentLinkedQueue<>();
    private Appender log4jAppender;
    private int consoleTaskId = -1;

    public DiscordBridge(Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.config = plugin.getConfig();
    }

    public void enable() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String token = config.getString("discord.token");
                String chatId = config.getString("discord.chat_channel");
                String consoleId = config.getString("discord.console_channel");

                if (token == null || token.isEmpty()) {
                    logger.severe("Discord token missing in config.yml.");
                    return;
                }

                jda = JDABuilder.createDefault(token).build().awaitReady();
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

        getChatChannel().sendMessage("🔴 Minecraft Server stopped.").queue();

        if (jda != null) jda.shutdown();
    }

    public TextChannel getChatChannel() { return chatChannel; }
    public TextChannel getConsoleChannel() { return consoleChannel; }
}
