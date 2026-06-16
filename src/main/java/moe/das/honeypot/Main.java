package moe.das.honeypot;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        var config = new Config();

        var jda = JDABuilder.createDefault(config.botToken(), GatewayIntent.GUILD_MESSAGES)
                .disableCache(
                        CacheFlag.VOICE_STATE,
                        CacheFlag.EMOJI,
                        CacheFlag.STICKER,
                        CacheFlag.SOUNDBOARD_SOUNDS,
                        CacheFlag.SCHEDULED_EVENTS
                )
                .addEventListeners(new MessageListener(config))
                .build();

        jda.awaitReady();
    }
}
