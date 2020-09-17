package com.w1sh.medusa.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.w1sh.medusa.data.responses.MessageEnum;
import com.w1sh.medusa.data.responses.Response;
import discord4j.core.object.Embed;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
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

/**
 * A services responsible for sending messages to discord channels, caching the messages sent and retrieving messages
 * from the resource bundle.
 *
 * The service can also handle fragmented {@link Response}, or basically multiple messages to be sent at the same time
 * ordered for a single event. For this a map is available which will store the sorted {@link Response} for a given
 * {@link String} which represents the id of the {@link MessageChannel} to send the messages on. This responses can then
 * be send by invoking {@link #flush(String)} with the key.
 *
 */
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

    /**
     * Queues the {@link Response} to handled when all fragments have been received.
     *
     * @param response The {@link Response} to queue.
     */
    public void queue(Response response) {
        final SortedSet<Response> responses = responseMap.getOrDefault(response.getChannelId(), new TreeSet<>());
        responses.add(response);
        responseMap.put(response.getChannelId(), responses);
    }

    /**
     * Creates and stores a {@link Message} if the {@link Response} received is a fragment. If not a fragment,
     * creates and immediately sends a {@link Message} to the specified {@link MessageChannel}.
     *
     * @param channelMono A {@link Mono} that contains the {@link MessageChannel} to send the {@link Message} in.
     * @param response A {@link Response} containing the information for creating the {@link Message}.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Message} created if present.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Message> sendOrQueue(Mono<MessageChannel> channelMono, Response response) {
        if(response.isFragment()) {
            queue(response);
            return Mono.empty();
        } else return send(channelMono, response.getEmbedCreateSpec());
    }

    /**
     * Sends a {@link Message} to the specified {@link MessageChannel}.
     *
     * @param channelMono A {@link Mono} that contains the {@link MessageChannel} to send the {@link Message} in.
     * @param embedCreateSpec The {@link EmbedCreateSpec} that represents the {@link Embed} to be created.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Message} created if present.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Message> send(Mono<MessageChannel> channelMono, Consumer<EmbedCreateSpec> embedCreateSpec) {
        return channelMono.flatMap(channel -> channel.createEmbed(embedCreateSpec))
                .doOnNext(m -> messageCache.put(m.getId().asString(), m));
    }

    /**
     * Sends a {@link Message} to the specified {@link MessageChannel}.
     *
     * @param channelMono A {@link Mono} that contains the {@link MessageChannel} to send the {@link Message} in.
     * @param message The content of the {@link Message} to be sent.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Message} created if present.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Message> send(Mono<MessageChannel> channelMono, String message) {
        return channelMono.flatMap(channel -> channel.createMessage(message))
                .doOnNext(m -> messageCache.put(m.getId().asString(), m));
    }

    /**
     * Sends a {@link Message} to the specified {@link MessageChannel}.
     *
     * @param channelMono A {@link Mono} that contains the {@link MessageChannel} to send the {@link Message} in.
     * @param messageEnum The message key from resource bundle that represents the text of the message that will be sent.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Message} created if present.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Message> send(Mono<MessageChannel> channelMono, MessageEnum messageEnum) {
        return channelMono.flatMap(channel -> channel.createMessage(getMessage(messageEnum.getMessageKey(), null)))
                .doOnNext(message -> messageCache.put(message.getId().asString(), message));
    }

    /**
     * Sends a {@link Message} to the specified {@link MessageChannel}.
     *
     * @param channelMono A {@link Mono} that contains the {@link MessageChannel} to send the {@link Message} in.
     * @param messageEnum The message key from resource bundle that represents the text of the message that will be sent.
     * @param args The arguments to replace in the message, if any exist.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Message} created if present.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Message> send(Mono<MessageChannel> channelMono, MessageEnum messageEnum, String... args) {
        return channelMono.flatMap(channel -> channel.createMessage(getMessage(messageEnum.getMessageKey(), args)))
                .doOnNext(message -> messageCache.put(message.getId().asString(), message));
    }

    /**
     * Removes all messages cached for {@link MessageChannel} with specified id and sends them all.
     *
     * @param id The id of the {@link MessageChannel}.
     * @see FluxSink#next(Object)
     */
    public void flush(String id){
        SortedSet<Response> responses = responseMap.remove(id);
        if(responses != null) {
            responses.forEach(fluxSink::next);
            responses.clear();
        }
    }

    /**
     * Retrieves the message with the specified key and arguments from the resources bundle.
     *
     * @param messageKey The hey of the message to retrieve.
     * @param args The arguments of the message to retrieve.
     * @return The message
     */
    private String getMessage(String messageKey, String[] args) {
        try {
            return messageSource.getMessage(messageKey, args, Locale.ENGLISH);
        } catch (NoSuchMessageException e) {
            log.error("Failed to retrieve message with key {}", messageKey);
            return null;
        }
    }

    public static String formatDuration(Long duration){
        return String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(duration),
                TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
    }
}
