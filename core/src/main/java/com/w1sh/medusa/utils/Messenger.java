package com.w1sh.medusa.utils;

import discord4j.core.object.entity.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.http.client.ClientException;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Component
public class Messenger {

    private static final Logger logger = LoggerFactory.getLogger(Messenger.class);

    public static final String BULLET = "\u2022";
    public static final String ZERO_WIDTH_SPACE = "\u200E";

    public static Mono<discord4j.core.object.entity.Message> send(MessageChannel channel, Consumer<EmbedCreateSpec> spec){
        return Mono.just(channel)
                .flatMap(c -> c.createEmbed(spec))
                .onErrorResume(ClientException.isStatusCode(HttpResponseStatus.FORBIDDEN.code()), err -> {
                    logger.error("Failed to send message, bot is not in the guild", err);
                    return Mono.empty();
                });
    }

    public static Mono<discord4j.core.object.entity.Message> send(MessageChannel channel, String content){
        return Mono.just(channel)
                .flatMap(c -> c.createMessage(content))
                .onErrorResume(ClientException.isStatusCode(HttpResponseStatus.FORBIDDEN.code()), err -> {
                    logger.error("Failed to send message, bot is not in the guild", err);
                    return Mono.empty();
                });
    }

    public static String formatDuration(Long duration){
        return String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(duration),
                TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
    }

    public static String progressBar(Long current, Long length){
        final String unicodeBar = "▬";
        final float percentage = (100f / length * current);
        int currentLengthInBars = (int) ((percentage / 100) * 18);
        return String.format("**%s**\t%s%s%s\t**%s**",
                formatDuration(current),
                unicodeBar.repeat(currentLengthInBars),
                "⚪",
                unicodeBar.repeat(18 - currentLengthInBars),
                formatDuration(length));
    }
}
