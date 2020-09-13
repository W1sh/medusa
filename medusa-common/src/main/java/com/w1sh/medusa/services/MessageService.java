package com.w1sh.medusa.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.w1sh.medusa.data.responses.MessageEnum;
import com.w1sh.medusa.data.responses.Response;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.UnicastProcessor;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Slf4j
@Component
public class MessageService {

    public static final String BULLET = "\u2022";
    public static final String ZERO_WIDTH_SPACE = "\u200E";


    private final Map<String, SortedSet<Response>> responseMap = new ConcurrentHashMap<>();
    private final FluxSink<Response> fluxSink;
    private final Cache<String, Message> messageCache;
    private final MessageSource messageSource;

    public MessageService(ResourceBundleMessageSource messageSource) {
        final FluxProcessor<Response, Response> fluxProcessor = UnicastProcessor.create();
        this.messageSource = messageSource;
        this.fluxSink = fluxProcessor.sink();
        this.messageCache = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofHours(2))
                .expireAfterWrite(Duration.ofHours(6))
                .build();

        fluxProcessor.map(x -> x)
                .doOnEach(responseSignal -> log.debug("Received signal {} in message processor", responseSignal.getType().toString()))
                .flatMap(response -> response.getMessageChannelMono()
                        .flatMap(messageChannel -> messageChannel.createEmbed(response.getEmbedCreateSpec())))
                .subscribe();
    }

    public void queue(Response response) {
        SortedSet<Response> responses;
        if (!responseMap.containsKey(response.getChannelId())) {
            responses = new TreeSet<>();
        } else {
            responses = responseMap.get(response.getChannelId());
        }
        responses.add(response);
        responseMap.put(response.getChannelId(), responses);
    }

    public Mono<Message> sendOrQueue(Mono<MessageChannel> channelMono, Response response) {
        if(response.isFragment()) {
            queue(response);
            return Mono.empty();
        } else return send(channelMono, response.getEmbedCreateSpec());
    }

    public Mono<Message> send(Mono<MessageChannel> channelMono, Consumer<EmbedCreateSpec> embedCreateSpec) {
        return channelMono.flatMap(channel -> channel.createEmbed(embedCreateSpec))
                .doOnNext(m -> messageCache.put(m.getId().asString(), m));
    }

    public Mono<Message> send(Mono<MessageChannel> channelMono, String message) {
        return channelMono.flatMap(channel -> channel.createMessage(message))
                .doOnNext(m -> messageCache.put(m.getId().asString(), m));
    }

    public Mono<Message> send(Mono<MessageChannel> channelMono, MessageEnum messageEnum) {
        return channelMono.flatMap(channel -> channel.createMessage(getMessage(messageEnum.getMessageKey(), null)))
                .doOnNext(message -> messageCache.put(message.getId().asString(), message));
    }

    public Mono<Message> send(Mono<MessageChannel> channelMono, MessageEnum messageEnum, String... args) {
        return channelMono.flatMap(channel -> channel.createMessage(getMessage(messageEnum.getMessageKey(), args)))
                .doOnNext(message -> messageCache.put(message.getId().asString(), message));
    }

    public void flush(String id){
        SortedSet<Response> responses = responseMap.getOrDefault(id, new TreeSet<>());
        responses.forEach(fluxSink::next);
        responses.clear();
    }

    private String getMessage(String messageKey, String[] args) {
        return messageSource.getMessage(messageKey, args, Locale.ENGLISH);
    }

    public static String formatDuration(Long duration){
        return String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(duration),
                TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
    }
}
