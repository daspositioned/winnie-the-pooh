package moe.das.honeypot;

import java.awt.*;

public class Config {
    private final String botToken;
    private final long channelId;
    private final long logChannelId;
    private final int deletionTimeMinutes;
    private final String embedTitle;
    private final String embedDescription;
    private final Color embedColor;

    public Config() {
        this.botToken = System.getenv("BOT_TOKEN");
        this.channelId = Long.parseLong(System.getenv("CHANNEL_ID"));
        this.logChannelId = Long.parseLong(System.getenv("LOG_CHANNEL_ID"));
        this.deletionTimeMinutes = Integer.parseInt(System.getenv("DELETION_TIME_MINUTES"));
        this.embedTitle = System.getenv("EMBED_TITLE");
        this.embedDescription = System.getenv("EMBED_DESCRIPTION");
        this.embedColor = Color.decode(System.getenv("EMBED_COLOR"));
    }

    public String botToken() {
        return botToken;
    }

    public long channelId() {
        return channelId;
    }

    public long logChannelId() {
        return logChannelId;
    }

    public int deletionTimeMinutes() {
        return deletionTimeMinutes;
    }

    public String embedTitle() {
        return embedTitle;
    }

    public String embedDescription() {
        return embedDescription;
    }

    public Color embedColor() {
        return embedColor;
    }
}
