package moe.das.honeypot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.awt.*;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MessageListener extends ListenerAdapter {
    private static final int MAX_TRACKED_MESSAGES = 5000;
    private static final int CLEANUP_DELAY_SECONDS = 10;

    private final Config config;
    private final Deque<TrackedMessage> trackedMessages = new ArrayDeque<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public MessageListener(Config config) {
        this.config = config;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.isFromGuild()) return;
        trackMessage(event);
        if (event.getChannel().getIdLong() != config.channelId()) return;

        var guild = event.getGuild();
        var member = event.getMember();
        var user = member.getUser();

        var dms = user.openPrivateChannel().complete();
        sendBanMessage(dms);

        member.ban(config.deletionTimeMinutes(), TimeUnit.MINUTES).reason("Triggered honeypot").queue(success -> {
            guild.unban(user).reason("Automatic honeypot unban").queue();
            scheduler.schedule(() -> deleteLeftoverMessages(guild, user), CLEANUP_DELAY_SECONDS, TimeUnit.SECONDS);

            var logChannel = guild.getTextChannelById(config.logChannelId());
            sendLogMessage(logChannel, user);
        });
    }

    private void trackMessage(MessageReceivedEvent event) {
        var message = new TrackedMessage(
                event.getAuthor().getIdLong(),
                event.getChannel().getIdLong(),
                event.getMessageIdLong()
        );

        synchronized (trackedMessages) {
            trackedMessages.addLast(message);
            if (trackedMessages.size() > MAX_TRACKED_MESSAGES) trackedMessages.removeFirst();
        }
    }

    private void sendBanMessage(PrivateChannel dms) {
        try {
            dms.sendMessageEmbeds(new EmbedBuilder()
                            .setColor(config.embedColor())
                            .setTitle(config.embedTitle())
                            .setDescription(config.embedDescription())
                            .build())
                    .complete();
        } catch (Exception exception) {
            // Do nothing, not all users can receive DMs
        }
    }

    private void sendLogMessage(TextChannel channel, User user) {
        channel.sendMessageEmbeds(new EmbedBuilder()
                .setTitle("Honeypot triggered")
                .setColor(config.embedColor())
                .setDescription(String.format(
                        "%s (%s) triggered honeypot and was soft-banned",
                        user.getAsMention(), user.getName()
                ))
                .build()).queue();
    }

    private void deleteLeftoverMessages(Guild guild, User user) {
        for (var message : getTrackedMessages(user)) {
            var channel = guild.getChannelById(GuildMessageChannel.class, message.channelId());
            if (channel == null) continue;

            try {
                channel.deleteMessageById(message.messageId())
                        .reason("Honeypot cleanup")
                        .queue(null, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE));
            } catch (Exception exception) {
                // Do nothing, the bot may have lost access to the channel
            }
        }
    }

    private List<TrackedMessage> getTrackedMessages(User user) {
        synchronized (trackedMessages) {
            return trackedMessages.stream()
                    .filter(message -> message.authorId() == user.getIdLong())
                    .toList();
        }
    }

    private record TrackedMessage(long authorId, long channelId, long messageId) {
    }
}
