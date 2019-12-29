package com.w1sh.medusa.utils;

import com.w1sh.medusa.core.data.TextMessage;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.http.client.ClientException;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Component
public class Messenger {

    private static final Logger logger = LoggerFactory.getLogger(Messenger.class);
    private static final FluxProcessor<TextMessage, TextMessage> messageProcessor = EmitterProcessor.create(false);
    private static final Integer bufferSize = 2;

    public static final String ZERO_WIDTH_SPACE = "\u200E";

    public static void queue(MessageCreateEvent event, String content){
        Mono.just(event)
                .flatMap(ev -> ev.getMessage().getChannel())
                .map(messageChannel -> new TextMessage(messageChannel, content, false))
                .subscribe(messageProcessor::onNext);
    }

    public static void flush(){
        messageProcessor.publish()
                .autoConnect()
                .bufferTimeout(2, Duration.ofSeconds(2))
                .flatMap(Flux::fromIterable)
                .flatMap(textMessage -> textMessage.getChannel().createMessage(textMessage.getContent()))
                .subscribe();
    }

    public static Mono<discord4j.core.object.entity.Message> send(MessageCreateEvent event, String content){
        return Mono.just(event)
                .flatMap(ev -> ev.getMessage().getChannel())
                .flatMap(channel -> send(channel, content));
    }

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
