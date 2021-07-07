package com.w1sh.medusa.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.w1sh.medusa.data.responses.MessageEnum;
import com.w1sh.medusa.data.responses.OutputEmbed;
import discord4j.core.object.Embed;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;
import reactor.cache.CacheMono;
import reactor.core.publisher.*;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * A services responsible for sending messages to discord channels, caching the messages sent and retrieving messages
 * from the resource bundle.
 *
 * The service can also handle fragmented {@link OutputEmbed}, or basically multiple messages to be sent at the same time
 * ordered for a single event. For this a map is available which will store the sorted {@link OutputEmbed} for a given
 * {@link String} which represents the id of the {@link MessageChannel} to send the messages on. This responses can then
 * be send by invoking {@link #flush(String)} with the key.
 *
 */
@Component
public final class MessageService {

    public static final String BULLET = "\u2022";
    public static final String ZERO_WIDTH_SPACE = "\u200E";
    private static final Logger log = LoggerFactory.getLogger(MessageService.class);

    private final Map<String, SortedSet<OutputEmbed>> responseMap = new ConcurrentHashMap<>();
    private final FluxSink<OutputEmbed> fluxSink;
    private final Cache<String, Tuple2<Message, OutputEmbed>> messageCache;
    private final MessageSource messageSource;
    private final ReactionService reactionService;

    public MessageService(ResourceBundleMessageSource messageSource, ReactionService reactionService) {
        final FluxProcessor<OutputEmbed, OutputEmbed> fluxProcessor = UnicastProcessor.create();
        this.reactionService = reactionService;
        this.messageSource = messageSource;
        this.fluxSink = fluxProcessor.sink();
        this.messageCache = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofHours(2))
                .expireAfterWrite(Duration.ofHours(6))
                .build();

        fluxProcessor.map(x -> x)
                .doOnEach(responseSignal -> log.debug("Received signal {} in message processor", responseSignal.getType().toString()))
                .flatMap(response -> response.getMessageChannelMono()
                        .flatMap(messageChannel -> messageChannel.createEmbed(response.getEmbedCreateSpec()))
                        .flatMap(message -> reactionService.addReactions(message, response.getReactions())))
                .subscribe();
    }

    public Mono<Tuple2<Message, OutputEmbed>> getCached(String messageId) {
        return CacheMono.lookup(key -> Mono.justOrEmpty(messageCache.getIfPresent(key))
                .map(Signal::next), messageId)
                .onCacheMissResume(Mono.empty())
                .andWriteWith((key, signal) -> Mono.fromRunnable(() -> Optional.ofNullable(signal.get()).ifPresent(value -> messageCache.put(key, value))));
    }

    public Mono<Message> update(String messageId, OutputEmbed outputEmbed) {
        return getCached(messageId)
                .flatMap(tuple -> tuple.getT1().edit(messageEditSpec -> messageEditSpec.setEmbed(outputEmbed.getEmbedCreateSpec())))
                .doOnNext(message -> messageCache.put(messageId, Tuples.of(message, outputEmbed)));
    }

    /**
     * Queues the {@link OutputEmbed} to handled when all fragments have been received.
     *
     * @param outputEmbed The {@link OutputEmbed} to queue.
     */
    public void queue(OutputEmbed outputEmbed) {
        final SortedSet<OutputEmbed> responses = responseMap.getOrDefault(outputEmbed.getChannelId(), new TreeSet<>());
        responses.add(outputEmbed);
        responseMap.put(outputEmbed.getChannelId(), responses);
    }

    /**
     * Creates and stores a {@link Message} if the {@link OutputEmbed} received is a fragment. If not a fragment,
     * creates and immediately sends a {@link Message} to the specified {@link MessageChannel}.
     *
     * @param channelMono A {@link Mono} that contains the {@link MessageChannel} to send the {@link Message} in.
     * @param outputEmbed A {@link OutputEmbed} containing the information for creating the {@link Message}.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Message} created if present.
     * If an error is received, it is emitted through the {@code Mono}.
     */
    public Mono<Void> sendOrQueue(Mono<MessageChannel> channelMono, OutputEmbed outputEmbed) {
        if(outputEmbed.isFragment()) {
            return Mono.fromRunnable(() -> queue(outputEmbed));
        } else return send(channelMono, outputEmbed);
    }

    public Mono<Void> send(Mono<MessageChannel> channelMono, OutputEmbed outputEmbed) {
        return channelMono.flatMap(channel -> channel.createEmbed(outputEmbed.getEmbedCreateSpec()))
                .doOnNext(m -> messageCache.put(m.getId().asString(), Tuples.of(m, outputEmbed)))
                .flatMap(message -> reactionService.addReactions(message, outputEmbed.getReactions()));
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
        return channelMono.flatMap(channel -> channel.createEmbed(embedCreateSpec));
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
        return channelMono.flatMap(channel -> channel.createMessage(message));
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
        return channelMono.flatMap(channel -> channel.createMessage(getMessage(messageEnum.getMessageKey(), null)));
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
        return channelMono.flatMap(channel -> channel.createMessage(getMessage(messageEnum.getMessageKey(), args)));
    }

    /**
     * Removes all messages cached for {@link MessageChannel} with specified id and sends them all.
     *
     * @param id The id of the {@link MessageChannel}.
     * @see FluxSink#next(Object)
     */
    public void flush(String id){
        SortedSet<OutputEmbed> respons = responseMap.remove(id);
        if(respons != null) {
            respons.forEach(fluxSink::next);
            respons.clear();
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
