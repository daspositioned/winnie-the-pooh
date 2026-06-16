package moe.das.honeypot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class MessageListener extends ListenerAdapter {
    private final Config config;

    public MessageListener(Config config) {
        this.config = config;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.isFromGuild()) return;
        if (event.getChannel().getIdLong() != config.channelId()) return;

        var guild = event.getGuild();
        var member = event.getMember();
        var user = member.getUser();

        var dms = user.openPrivateChannel().complete();
        sendBanMessage(dms);

        member.ban(config.deletionTimeMinutes(), TimeUnit.MINUTES).reason("Triggered honeypot").queue(success -> {
            guild.unban(user).reason("Automatic honeypot unban").queue();

            var logChannel = guild.getTextChannelById(config.logChannelId());
            sendLogMessage(logChannel, user);
        });
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
}
