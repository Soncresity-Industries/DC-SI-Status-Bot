package dev.soncresityindustries.StatusBot;

import dev.soncresityindustries.StatusBot.storage.Service;
import dev.soncresityindustries.StatusBot.storage.StatusStorage;
import dev.soncresityindustries.StatusBot.util.EmbedUtils;
import dev.soncresityindustries.StatusBot.util.LogUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatusUpdateManager {

    public static void updateStatusMessages(JDA jda, StatusStorage storage) {
        String channelId;
        try {
            channelId = Config.get().getStatus().getStatusChannelId();
        } catch (Exception e) {
            LogUtils.logFatalException("Failed to load status channel ID from config", e);
            return;
        }

        MessageChannel channel;
        try {
            channel = jda.getChannelById(MessageChannel.class, channelId);
        } catch (Exception e) {
            LogUtils.logFatalException("Failed to retrieve status channel by ID: " + channelId, e);
            return;
        }

        channel.getIterableHistory()
                .takeAsync(100)
                .thenAccept(messages -> {
                    for (Message msg : messages) {
                        if (msg.getAuthor().isBot()) {
                            msg.delete().queue(null, e -> {});
                        }
                    }

                    postStatusEmbeds(channel, storage);
                })
                .exceptionally(e -> {
                    LogUtils.logFatalException("Failed to retrieve or delete messages in status channel", e);
                    return null;
                });
    }

    private static void postStatusEmbeds(MessageChannel channel, StatusStorage storage) {
        // Group services by parent
        Map<String, List<Service>> childrenMap = new HashMap<>();
        List<Service> parents = new ArrayList<>();

        for (Service service : storage.getAllServices()) {
            if (service.getParentId() != null) {
                childrenMap.computeIfAbsent(service.getParentId(), k -> new ArrayList<>()).add(service);
            } else {
                parents.add(service);
            }
        }

        // Send embed for each parent (or standalone service)
        for (Service parent : parents) {
            List<Service> children = childrenMap.getOrDefault(parent.getServiceId(), List.of());
            EmbedBuilder embed = buildStatusEmbed(parent, children, channel);

            channel.sendMessageEmbeds(embed.build()).queue(msg -> {
                if (channel instanceof NewsChannel news) {
                    news.crosspostMessageById(msg.getId()).queue(null, e -> {});
                }
            });
        }

        boolean majorOutage = false;
        for (Service service : storage.getAllServices()) {
            if (service.getStatus().contains("Major Outage")) {
                try {
                    if (channel instanceof NewsChannel news) {
                        news.getManager().setName(Config.get().getStatus().getMajorOutageChannelName()).queue();
                    } else if (channel instanceof TextChannel text) {
                        text.getManager().setName(Config.get().getStatus().getMajorOutageChannelName()).queue();
                    }
                    majorOutage = true;
                    break;
                } catch (IOException e) {
                    LogUtils.logException("Error updating channel name for major outage", e);
                }
            }
        }

        if (majorOutage) return;
        boolean partialOutage = false;
        for (Service service : storage.getAllServices()) {
            if (service.getStatus().contains("Partial Outage")) {
                try {
                    if (channel instanceof NewsChannel news) {
                        news.getManager().setName(Config.get().getStatus().getPartialOutageChannelName()).queue();
                    } else if (channel instanceof TextChannel text) {
                        text.getManager().setName(Config.get().getStatus().getPartialOutageChannelName()).queue();
                    }
                    partialOutage = true;
                    break;
                } catch (IOException e) {
                    LogUtils.logException("Error updating channel name for major outage", e);
                }
            }
        }

        if (partialOutage) return;
        boolean maintenance = false;
        for (Service service : storage.getAllServices()) {
            if (service.getStatus().contains("Maintenance")) {
                try {
                    if (channel instanceof NewsChannel news) {
                        news.getManager().setName(Config.get().getStatus().getMaintenanceChannelName()).queue();
                    } else if (channel instanceof TextChannel text) {
                        text.getManager().setName(Config.get().getStatus().getMaintenanceChannelName()).queue();
                    }
                    maintenance = true;
                    break;
                } catch (IOException e) {
                    LogUtils.logException("Error updating channel name for major outage", e);
                }
            }
        }

        if (maintenance) return;
        try {
            if (channel instanceof NewsChannel news) {
                news.getManager().setName(Config.get().getStatus().getPartialOutageChannelName()).queue();
            } else if (channel instanceof TextChannel text) {
                text.getManager().setName(Config.get().getStatus().getPartialOutageChannelName()).queue();
            }
        } catch (IOException e) {
            LogUtils.logException("Error updating channel name for major outage", e);
        }
    }

    private static EmbedBuilder buildStatusEmbed(Service parent, List<Service> children, MessageChannel channel) {
        EmbedBuilder embed = EmbedUtils.createDefault()
                .setTitle("Service Status - " + parent.getDisplayName());

        // Determine color
        Color color = determineColor(parent, children);
        embed.setColor(color);

        // Parent line
        embed.appendDescription("**" + parent.getDisplayName() + "**\n");
        embed.appendDescription(parent.getDescription() + "\n");
        if (parent.getOutageDescription() != null && !parent.getOutageDescription().isEmpty()) {
            embed.appendDescription("> " + parent.getStatus() + " - " + parent.getOutageDescription() + "\n\n");
        } else embed.appendDescription("> " + parent.getStatus() + "\n\n");

        // Child lines
        if (!children.isEmpty()) {
            embed.appendDescription("**Sub-services:**\n");
            for (Service child : children) {
                if (child.getOutageDescription() != null && !child.getOutageDescription().isEmpty()) {
                    embed.appendDescription("\n> **" + child.getDisplayName() + "**\n> " + child.getDescription() + "\n> " + child.getStatus() + " - " + child.getOutageDescription() + "\n");
                    continue;
                }
                embed.appendDescription("\n> **" + child.getDisplayName() + "**\n> " + child.getDescription() + "\n> " + child.getStatus() + "\n");
            }
        }

        return embed;
    }

    private static Color determineColor(Service parent, List<Service> children) {
        List<Service> all = new ArrayList<>(children);
        all.add(parent);

        boolean anyDown = all.stream().anyMatch(s -> s.getStatus().contains("Major Outage"));
        boolean anyPartial = all.stream().anyMatch(s -> s.getStatus().contains("Partial Outage"));
        if (!anyPartial) anyPartial = all.stream().anyMatch(s -> s.getStatus().contains("Degraded Performance"));
        boolean anyMaintenance = all.stream().anyMatch(s -> s.getStatus().contains("Maintenance"));

        if (anyDown) return Color.RED;
        if (anyPartial) return Color.YELLOW;
        if (anyMaintenance) return EmbedUtils.DEFAULT_COLOR;
        return Color.GREEN;
    }
}